/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        navy: {
          900: '#0B132B',
          800: '#111827',
          700: '#1F2937',
        },
        brand: {
          blue: '#2563EB',
          emerald: '#10B981',
          charcoal: '#1E293B',
          amber: '#F59E0B',
          danger: '#EF4444',
          bg: '#F8FAFC',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
