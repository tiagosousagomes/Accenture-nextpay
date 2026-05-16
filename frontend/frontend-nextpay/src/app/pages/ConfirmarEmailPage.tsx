import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { MailCheck, ArrowLeft } from "lucide-react";
import { toast } from "sonner";

export const ConfirmarEmailPage: React.FC = () => {
  const [codigo, setCodigo] = useState("");
  const [erro, setErro] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();

  const email = location.state?.email;

  const confirmarCodigo = async () => {
    if (!codigo.trim()) {
      setErro("Digite o código recebido no e-mail.");
      return;
    }

    setLoading(true);
    setErro("");

    try {
      const response = await fetch("http://localhost:8080/api/usuarios/confirmar-email", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          email,
          codigo
        })
      });

      if (!response.ok) {
        setErro("Código de confirmação inválido.");
        return;
      }

      toast.success("Cadastro realizado com sucesso!");
      navigate("/login");

    } catch (error) {
      setErro("Erro ao confirmar e-mail. Tente novamente.");
    } finally {
      setLoading(false);
    }
  };

  const cancelar = () => {
    navigate("/cadastro");
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-petroleo-900 via-petroleo-700 to-petroleo-950 flex items-center justify-center p-6">
      <div className="bg-white rounded-3xl shadow-2xl w-full max-w-md p-8">

        <div className="flex flex-col items-center text-center mb-8">
          <div className="w-20 h-20 bg-petroleo-100 rounded-full flex items-center justify-center mb-4">
            <MailCheck className="text-petroleo-700" size={42} />
          </div>

          <h1 className="text-3xl font-bold text-gray-900">
            Confirmação de Email
          </h1>

          <p className="text-gray-500 mt-2">
            Enviamos um código de confirmação para:
          </p>

          <p className="font-semibold text-petroleo-700 mt-1">
            {email}
          </p>
        </div>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Código de confirmação
            </label>

            <input
              type="text"
              maxLength={6}
              value={codigo}
              onChange={(e) => setCodigo(e.target.value)}
              placeholder="Digite os 6 dígitos"
              className="w-full px-4 py-3 border border-gray-300 rounded-xl text-center text-2xl font-bold tracking-widest focus:outline-none focus:ring-2 focus:ring-petroleo-600"
            />
          </div>

          {erro && (
            <div className="bg-red-50 border border-red-200 text-red-600 rounded-xl px-4 py-3 text-sm font-medium">
              {erro}
            </div>
          )}

          <button
            onClick={confirmarCodigo}
            disabled={loading}
            className="w-full bg-petroleo-900 text-white py-3 rounded-xl font-bold hover:bg-petroleo-950 transition disabled:opacity-60"
          >
            {loading ? "Confirmando..." : "Confirmar Cadastro"}
          </button>

          <button
            onClick={cancelar}
            className="w-full bg-gray-100 text-gray-700 py-3 rounded-xl font-bold hover:bg-gray-200 transition flex items-center justify-center gap-2"
          >
            <ArrowLeft size={18} />
            Cancelar e voltar
          </button>
        </div>
      </div>
    </div>
  );
};