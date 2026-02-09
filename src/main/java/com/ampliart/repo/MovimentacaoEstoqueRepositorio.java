package com.ampliart.repo;

import com.ampliart.dominio.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoEstoqueRepositorio extends JpaRepository<MovimentacaoEstoque, Long> {
}
