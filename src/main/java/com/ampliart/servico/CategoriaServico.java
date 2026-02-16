package com.ampliart.servico;

import com.ampliart.dominio.Categoria;
import com.ampliart.repo.CategoriaRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaServico {

    public static final String NOME_CATEGORIA_PADRAO = "Sem categoria";

    private final CategoriaRepositorio categoriaRepositorio;

    public CategoriaServico(CategoriaRepositorio categoriaRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
    }

    public List<Categoria> listar() {
        return categoriaRepositorio.findAll();
    }

    public List<Categoria> listarParaCadastroProduto() {
        return categoriaRepositorio.findAll().stream()
                .filter(categoria -> !ehCategoriaPadrao(categoria))
                .toList();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria nao encontrada"));
    }

    public Categoria salvar(Categoria categoria) {
        return categoriaRepositorio.save(categoria);
    }

    public boolean ehCategoriaPadrao(Categoria categoria) {
        if (categoria == null || categoria.getNome() == null) {
            return false;
        }
        return NOME_CATEGORIA_PADRAO.equalsIgnoreCase(categoria.getNome().trim());
    }
}
