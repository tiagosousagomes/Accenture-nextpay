package acc.br.nextpay.service;

import acc.br.nextpay.model.Endereco;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ViaCepService {

    public Endereco buscarEnderecoPorCep(String cep) {
        String url = "https://viacep.com.br/ws/" + cep + "/json/";
        RestTemplate restTemplate = new RestTemplate();

        try {
            Endereco endereco = restTemplate.getForObject(url, Endereco.class);
            if (endereco != null && endereco.getCep() != null) {
                // Remove o hífen para passar na validação de 8 dígitos
                endereco.setCep(endereco.getCep().replace("-", ""));
            }
            return endereco;
        } catch (Exception e) {
            return null;
        }
    }
}