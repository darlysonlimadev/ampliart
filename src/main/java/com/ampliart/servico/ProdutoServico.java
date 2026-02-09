package com.ampliart.servico;

import com.ampliart.dominio.Produto;
import com.ampliart.repo.ProdutoRepositorio;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoServico {

    private final ProdutoRepositorio produtoRepositorio;

    public ProdutoServico(ProdutoRepositorio produtoRepositorio) {
        this.produtoRepositorio = produtoRepositorio;
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
        boolean temNome = nome != null && !nome.isBlank();
        if (temNome && categoriaId != null) {
            return produtoRepositorio.findByNomeContainingIgnoreCaseAndCategoriaId(nome, categoriaId);
        }
        if (temNome) {
            return produtoRepositorio.findByNomeContainingIgnoreCase(nome);
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
        try {
            return produtoRepositorio.save(produto);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Codigo ja cadastrado");
        }
    }
}
