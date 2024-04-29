package io.github.felipeecp.mscartoes.infra.repository;

import io.github.felipeecp.mscartoes.domain.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface CartaRepository extends JpaRepository<Cartao, Long> {


    List<Cartao> findByRendaLessThanEqual(BigDecimal renda);
}
