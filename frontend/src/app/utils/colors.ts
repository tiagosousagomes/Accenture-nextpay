// Azul Petróleo - Cor principal sofisticada que passa confiança
export const colors = {
  primary: {
    50: '#f0fdfa',
    100: '#ccfbf1',
    200: '#99f6e4',
    300: '#5eead4',
    400: '#2dd4bf',
    500: '#14b8a6', // Azul petróleo principal
    600: '#0d9488',
    700: '#0f766e',
    800: '#115e59',
    900: '#134e4a',
  },
  secondary: {
    50: '#faf5ff',
    100: '#f3e8ff',
    200: '#e9d5ff',
    300: '#d8b4fe',
    400: '#c084fc',
    500: '#a855f7',
    600: '#9333ea',
    700: '#7e22ce',
    800: '#6b21a8',
    900: '#581c87',
  },
  // Gradientes comuns
  gradients: {
    primary: 'from-teal-600 to-teal-700',
    secondary: 'from-teal-500 via-cyan-600 to-teal-700',
    accent: 'from-purple-600 to-pink-600',
  },
};

// Classes Tailwind para gradientes
export const gradientClasses = {
  primary: 'bg-gradient-to-r from-teal-600 to-teal-700',
  primaryHover: 'hover:from-teal-700 hover:to-teal-800',
  secondary: 'bg-gradient-to-r from-teal-500 via-cyan-600 to-teal-700',
  accent: 'bg-gradient-to-br from-teal-600 to-cyan-700',
  sidebar: 'bg-gradient-to-b from-teal-700 to-teal-900',
  card: 'bg-gradient-to-br from-teal-50 to-cyan-50',
  text: 'bg-gradient-to-r from-teal-600 to-cyan-600 bg-clip-text text-transparent',
};
