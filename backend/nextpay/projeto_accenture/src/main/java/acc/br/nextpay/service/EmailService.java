package acc.br.nextpay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarEmail(String destino, String assunto, String mensagem) {

        SimpleMailMessage email = new SimpleMailMessage();

        email.setTo(destino);
        email.setSubject(assunto);
        email.setText(mensagem);

        mailSender.send(email);
    }
}