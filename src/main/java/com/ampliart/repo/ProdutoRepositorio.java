package com.ampliart.repo;

import com.ampliart.dominio.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepositorio extends JpaRepository<Produto, Long> {
    Optional<Produto> findByCodigo(String codigo);
    List<Produto> findByNomeContainingIgnoreCase(String nome);
    List<Produto> findByNomeContainingIgnoreCaseAndAtivo(String nome, Boolean ativo);
    List<Produto> findByAtivo(Boolean ativo);
    List<Produto> findByNomeContainingIgnoreCaseAndCategoriaId(String nome, Long categoriaId);
    List<Produto> findByCategoriaId(Long categoriaId);
}
