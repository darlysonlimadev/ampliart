package com.ampliart.repo;

import com.ampliart.dominio.Orcamento;
import com.ampliart.dominio.StatusOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrcamentoRepositorio extends JpaRepository<Orcamento, Long> {
    List<Orcamento> findByStatus(StatusOrcamento status);

    @Query("select o from Orcamento o where o.status = :status and o.dataConclusao is not null")
    List<Orcamento> buscarConcluidos(@Param("status") StatusOrcamento status);

    @Query("select o from Orcamento o where o.status = :status and o.dataConclusao between :inicio and :fim")
    List<Orcamento> buscarConcluidosPeriodo(@Param("status") StatusOrcamento status,
                                            @Param("inicio") LocalDateTime inicio,
                                            @Param("fim") LocalDateTime fim);

    @Query("select o from Orcamento o where o.dataCadastro between :inicio and :fim")
    List<Orcamento> buscarPorDataCadastro(@Param("inicio") LocalDateTime inicio,
                                          @Param("fim") LocalDateTime fim);

    @Query("select o from Orcamento o where o.status = :status and o.dataCadastro between :inicio and :fim")
    List<Orcamento> buscarPorStatusEDataCadastro(@Param("status") StatusOrcamento status,
                                                 @Param("inicio") LocalDateTime inicio,
                                                 @Param("fim") LocalDateTime fim);
}
