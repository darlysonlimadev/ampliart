package com.ampliart.servico;

import com.ampliart.dominio.Categoria;
import com.ampliart.dominio.Produto;
import com.ampliart.repo.CategoriaRepositorio;
import com.ampliart.repo.ProdutoRepositorio;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoServico {

    private final ProdutoRepositorio produtoRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;

    public ProdutoServico(ProdutoRepositorio produtoRepositorio, CategoriaRepositorio categoriaRepositorio) {
        this.produtoRepositorio = produtoRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
    }

    public List<Produto> listar(String nome, String codigo, Boolean ativo) {
        if (codigo != null && !codigo.isBlank()) {
            Optional<Produto> produto = produtoRepositorio.findByCodigo(codigo);
            return produto.map(List::of).orElseGet(List::of);
        }
        if (nome != null && !nome.isBlank() && ativo != null) {
            return produtoRepositorio.findByNomeContainingIgnoreCaseAndAtivo(nome, ativo);
        }
        if (nome != null && !nome.isBlank()) {
            return produtoRepositorio.findByNomeContainingIgnoreCase(nome);
        }
        if (ativo != null) {
            return produtoRepositorio.findByAtivo(ativo);
        }
        return produtoRepositorio.findAll();
    }

    public List<Produto> listarPorNomeECategoria(String nome, Long categoriaId) {
        String termoBusca = nome != null ? nome.trim() : null;
        boolean temNome = termoBusca != null && !termoBusca.isBlank();

        if (temNome) {
            Optional<Produto> produtoPorCodigo = produtoRepositorio.findByCodigo(termoBusca);
            if (produtoPorCodigo.isPresent()) {
                return List.of(produtoPorCodigo.get());
            }
        }

        if (temNome && categoriaId != null) {
            return produtoRepositorio.findByNomeContainingIgnoreCaseAndCategoriaId(termoBusca, categoriaId);
        }
        if (temNome) {
            return produtoRepositorio.findByNomeContainingIgnoreCase(termoBusca);
        }
        if (categoriaId != null) {
            return produtoRepositorio.findByCategoriaId(categoriaId);
        }
        return produtoRepositorio.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepositorio.findById(id).orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
    }

    public Produto buscarPorCodigo(String codigo) {
        return produtoRepositorio.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
    }

    @Transactional
    public Produto salvar(Produto produto) {
        Categoria categoria = validarCategoriaObrigatoria(produto);
        try {
            if (produto.getId() == null) {
                produto.setCategoria(categoria);
                return produtoRepositorio.saveAndFlush(produto);
            }

            Produto existente = buscarPorId(produto.getId());
            existente.setNome(produto.getNome());
            existente.setDescricao(produto.getDescricao());
            existente.setCodigo(produto.getCodigo());
            existente.setCategoria(categoria);
            existente.setPrecoCompra(produto.getPrecoCompra());
            existente.setPrecoVenda(produto.getPrecoVenda());
            existente.setQuantidadeEstoque(produto.getQuantidadeEstoque());
            existente.setAtivo(produto.getAtivo());
            return produtoRepositorio.saveAndFlush(existente);
        } catch (DataIntegrityViolationException ex) {
            throw traduzirErroDeIntegridade(ex);
        }
    }

    private Categoria validarCategoriaObrigatoria(Produto produto) {
        if (produto.getCategoria() == null || produto.getCategoria().getId() == null) {
            throw new IllegalArgumentException("Selecione uma categoria");
        }

        Long categoriaId = produto.getCategoria().getId();
        Categoria categoria = categoriaRepositorio.findById(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoria invalida"));

        String nomeCategoria = categoria.getNome() != null ? categoria.getNome().trim() : "";
        if (CategoriaServico.NOME_CATEGORIA_PADRAO.equalsIgnoreCase(nomeCategoria)) {
            throw new IllegalArgumentException("Selecione uma categoria valida");
        }

        return categoria;
    }

    private IllegalArgumentException traduzirErroDeIntegridade(DataIntegrityViolationException ex) {
        Throwable causa = ex.getMostSpecificCause();
        String mensagem = causa != null ? causa.getMessage() : ex.getMessage();
        String texto = mensagem != null ? mensagem.toLowerCase() : "";

        if (texto.contains("uk_produto_codigo") || texto.contains("(codigo)") || texto.contains("codigo")) {
            return new IllegalArgumentException("Codigo ja cadastrado");
        }
        return new IllegalArgumentException("Nao foi possivel salvar o produto");
    }
}
