package io.github.felipeecp.mscartoes.application;

import io.github.felipeecp.mscartoes.domain.Cartao;
import io.github.felipeecp.mscartoes.infra.repository.CartaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaRepository repository;

    @Transactional
    public Cartao save(Cartao cartao) {
        return repository.save(cartao);
    }

    public List<Cartao> getCartoesRendaMenorIgual(Long renda){
        var rendaBigDecimal = BigDecimal.valueOf(renda);
        return repository.findByRendaLessThanEqual(rendaBigDecimal);
    }


    public List<Cartao> getAllCartoes() {
        return repository.findAll();
    }
}
