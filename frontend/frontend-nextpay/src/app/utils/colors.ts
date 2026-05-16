// NextPay brand colors
export const colors = {
  primary: {
    50: '#EDE9FE',
    100: '#DDD6FE',
    200: '#C4B5FD',
    300: '#A78BFA',
    400: '#8A2DFD', // Roxo Principal
    500: '#7B28EC',
    600: '#5E57FD', // Roxo Suave
    700: '#4D63FE', // Azul Médio
    800: '#3B4FD0',
    900: '#0329AB', // Azul Profundo
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
    primary: 'from-petroleo-900 to-petroleo-700',
    secondary: 'from-petroleo-900 via-petroleo-600 to-petroleo-700',
    accent: 'from-petroleo-400 to-petroleo-600',
  },
};

// Classes Tailwind para gradientes
export const gradientClasses = {
  primary: 'bg-gradient-to-r from-petroleo-900 to-petroleo-700',
  primaryHover: 'hover:from-petroleo-950 hover:to-petroleo-800',
  secondary: 'bg-gradient-to-r from-petroleo-900 via-petroleo-600 to-petroleo-700',
  accent: 'bg-gradient-to-br from-petroleo-900 to-petroleo-700',
  sidebar: 'bg-gradient-to-b from-petroleo-900 to-petroleo-950',
  card: 'bg-gradient-to-br from-petroleo-50 to-petroleo-100',
  text: 'bg-gradient-to-r from-petroleo-900 to-petroleo-600 bg-clip-text text-transparent',
};
