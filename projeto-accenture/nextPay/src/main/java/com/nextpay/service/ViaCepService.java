package com.nextpay.service;

import com.nextpay.dto.ViaCepResponse;
import com.nextpay.exception.BusinessException;
import com.nextpay.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class ViaCepService {

    private static final String VIA_CEP_URL = "https://viacep.com.br/ws/{cep}/json/";

    private final RestClient restClient = RestClient.create();

    public ViaCepResponse buscar(String cep) {
        String cepNormalizado = normalizar(cep);
        ViaCepResponse resp;
        try {
            resp = restClient.get()
                    .uri(VIA_CEP_URL, cepNormalizado)
                    .retrieve()
                    .body(ViaCepResponse.class);
        } catch (RestClientException ex) {
            throw new BusinessException("Falha ao consultar ViaCEP: " + ex.getMessage());
        }
        if (resp == null || resp.isErro()) {
            throw new ResourceNotFoundException("CEP não encontrado: " + cep);
        }
        return resp;
    }

    private String normalizar(String cep) {
        if (cep == null) {
            throw new BusinessException("CEP não informado");
        }
        String somenteDigitos = cep.replaceAll("\\D", "");
        if (somenteDigitos.length() != 8) {
            throw new BusinessException("CEP inválido: " + cep);
        }
        return somenteDigitos;
    }
}
