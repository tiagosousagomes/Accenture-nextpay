package acc.br.nextpay.ai;

import java.util.List;

public record DocumentChunk(String content, List<Float> embedding) {
}
