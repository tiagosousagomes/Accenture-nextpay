package com.nextpay.config;

import com.nextpay.entity.Cliente;
import com.nextpay.entity.RegraCashback;
import com.nextpay.entity.Transacao;
import com.nextpay.repository.RegraCashbackRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedRegrasCashback(RegraCashbackRepository repo) {
        return args -> {
            if (repo.count() > 0) return;

            // ALIMENTACAO
            repo.save(novaRegra(Transacao.CategoriaTransacao.ALIMENTACAO, Cliente.NivelCliente.BRONZE, "1.0"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.ALIMENTACAO, Cliente.NivelCliente.PRATA, "2.0"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.ALIMENTACAO, Cliente.NivelCliente.OURO, "3.5"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.ALIMENTACAO, Cliente.NivelCliente.PLATINA, "5.0"));

            // MARKETPLACE
            repo.save(novaRegra(Transacao.CategoriaTransacao.MARKETPLACE, Cliente.NivelCliente.BRONZE, "2.0"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.MARKETPLACE, Cliente.NivelCliente.PRATA, "3.0"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.MARKETPLACE, Cliente.NivelCliente.OURO, "5.0"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.MARKETPLACE, Cliente.NivelCliente.PLATINA, "7.5"));

            // TRANSPORTE
            repo.save(novaRegra(Transacao.CategoriaTransacao.TRANSPORTE, Cliente.NivelCliente.BRONZE, "0.5"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.TRANSPORTE, Cliente.NivelCliente.PRATA, "1.0"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.TRANSPORTE, Cliente.NivelCliente.OURO, "2.0"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.TRANSPORTE, Cliente.NivelCliente.PLATINA, "3.0"));

            // LAZER
            repo.save(novaRegra(Transacao.CategoriaTransacao.LAZER, Cliente.NivelCliente.BRONZE, "1.0"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.LAZER, Cliente.NivelCliente.PRATA, "1.5"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.LAZER, Cliente.NivelCliente.OURO, "2.5"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.LAZER, Cliente.NivelCliente.PLATINA, "4.0"));

            // SERVICOS
            repo.save(novaRegra(Transacao.CategoriaTransacao.SERVICOS, Cliente.NivelCliente.BRONZE, "0.5"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.SERVICOS, Cliente.NivelCliente.PRATA, "1.0"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.SERVICOS, Cliente.NivelCliente.OURO, "1.5"));
            repo.save(novaRegra(Transacao.CategoriaTransacao.SERVICOS, Cliente.NivelCliente.PLATINA, "2.5"));
        };
    }

    private RegraCashback novaRegra(Transacao.CategoriaTransacao cat, Cliente.NivelCliente nivel, String pct) {
        return RegraCashback.builder()
                .categoria(cat)
                .nivelMinimo(nivel)
                .percentual(new BigDecimal(pct))
                .ativo(true)
                .build();
    }
}
