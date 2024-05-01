package io.github.felipeecp.msavaliadorcredito.application;

import feign.FeignException;
import io.github.felipeecp.msavaliadorcredito.application.exceptions.DadosClienteNotFoundException;
import io.github.felipeecp.msavaliadorcredito.application.exceptions.ErroComunicacaoMicroserviceException;
import io.github.felipeecp.msavaliadorcredito.application.exceptions.ErroSolicitacaoCartaoException;
import io.github.felipeecp.msavaliadorcredito.domain.model.*;
import io.github.felipeecp.msavaliadorcredito.infra.clients.CartoesResourceClient;
import io.github.felipeecp.msavaliadorcredito.infra.clients.ClienteResourceClient;
import io.github.felipeecp.msavaliadorcredito.infra.mq.SolicitacaoEmissaoCartaoPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {
    private final ClienteResourceClient clienteClient;
    private final CartoesResourceClient cartoesClient;
    private final SolicitacaoEmissaoCartaoPublisher emissaoCartaoPublisher;

    public SituacaoCliente obterSituacaoCliente(String cpf) throws DadosClienteNotFoundException, ErroComunicacaoMicroserviceException {
        try {
            ResponseEntity<DadosCliente> dadosClienteResponse = clienteClient.dadosCliente(cpf);
            ResponseEntity<List<CartaoCliente>> cartoesResponse = cartoesClient.getCatoesByCliente(cpf);

            return SituacaoCliente.builder()
                    .cliente(dadosClienteResponse.getBody())
                    .cartoes(cartoesResponse.getBody())
                    .build();

        }catch (FeignException.FeignClientException e) {
            int status = e.status();
            if(HttpStatus.NOT_FOUND.value() == status){
                throw new DadosClienteNotFoundException();
            }
            throw new ErroComunicacaoMicroserviceException(e.getMessage(),status);
        }
    }

    public RetornoAvaliacaoCliente realizarAvaliacao(String cpf, Long renda) throws DadosClienteNotFoundException, ErroComunicacaoMicroserviceException{
        try{
            ResponseEntity<DadosCliente> dadosClienteResponse = clienteClient.dadosCliente(cpf);
            ResponseEntity<List<Cartao>> cartoesResponse = cartoesClient.getCartoesRendaMaxima(renda);

            List<Cartao> cartoes = cartoesResponse.getBody();

            List<CartaoAprovado> cartoesAprovados = cartoes.stream()
                    .map(
                            cartao -> getCartoesAprovado(cartao, dadosClienteResponse)
                    ).collect(Collectors.toList());

            return new RetornoAvaliacaoCliente(cartoesAprovados);
        }catch (FeignException.FeignClientException e) {
            int status = e.status();
            if(HttpStatus.NOT_FOUND.value() == status){
                throw new DadosClienteNotFoundException();
            }
            throw new ErroComunicacaoMicroserviceException(e.getMessage(),status);
        }
    }

    private static CartaoAprovado getCartoesAprovado(Cartao cartao, ResponseEntity<DadosCliente> dadosClienteResponse) {
        DadosCliente dadosCliente = dadosClienteResponse.getBody();

        BigDecimal limiteBasico = cartao.getLimiteBasico();
        BigDecimal idadeBD = BigDecimal.valueOf(dadosCliente.getIdade());
        var fator = idadeBD.divide(BigDecimal.valueOf(10));
        BigDecimal limiteAprovado = fator.multiply(limiteBasico);

        CartaoAprovado aprovado = new CartaoAprovado();
        aprovado.setCartao(cartao.getNome());
        aprovado.setBandeira(cartao.getBandeira());
        aprovado.setLimiteAprovado(limiteAprovado);

        return aprovado;
    }

    public ProtocoloSolicitacaoCartao solicitarEmissaoCartao(DadosSolicitacaoEmissaoCartao dados){
        try {
            emissaoCartaoPublisher.solicitarCartao(dados);
            var protocolo = UUID.randomUUID().toString();
            return new ProtocoloSolicitacaoCartao(protocolo);
        }catch (Exception e){
            throw new ErroSolicitacaoCartaoException(e.getMessage());
        }
    }

}
