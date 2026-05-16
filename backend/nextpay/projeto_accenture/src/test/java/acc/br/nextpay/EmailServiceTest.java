package acc.br.nextpay;

import acc.br.nextpay.service.EmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private EmailService emailService;

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
        // Injeta o mock do JavaMailSender no campo privado do EmailService
        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
    }

    @Test
    void deveEnviarEmailComSucessoEMontarObjetoCorretamente() {
        // Dados de entrada do teste
        String destino = "cliente@nextpay.com";
        String assunto = "Bem-vindo ao NextPay";
        String mensagem = "Seu cadastro foi realizado com sucesso!";

        // Capturador para interceptar o SimpleMailMessage que será criado dentro do service
        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Execução do método sob teste
        emailService.enviarEmail(destino, assunto, mensagem);

        // Verifica se o método send do JavaMailSender foi chamado exatamente 1 vez
        verify(mailSender, times(1)).send(mailCaptor.capture());

        // Recupera o objeto real que foi passado para o método send
        SimpleMailMessage emailEnviado = mailCaptor.getValue();

        // Validações dos campos do e-mail montado
        Assertions.assertNotNull(emailEnviado);
        Assertions.assertNotNull(emailEnviado.getTo());
        Assertions.assertEquals(destino, emailEnviado.getTo()[0]);
        Assertions.assertEquals(assunto, emailEnviado.getSubject());
        Assertions.assertEquals(mensagem, emailEnviado.getText());
    }
}