import { createTheme, type ThemeOptions } from '@mui/material/styles'

const shared: ThemeOptions = {
  typography: {
    fontFamily: '"Roboto Flex", system-ui, sans-serif',
    h4: { fontWeight: 700 },
    h5: { fontWeight: 700 },
    h6: { fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  shape: { borderRadius: 12 },
  components: {
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 20 },
      },
    },
    MuiPaper: {
      styleOverrides: {
        rounded: { borderRadius: 16 },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: { borderRadius: 16 },
      },
    },
  },
}

export const lightTheme = createTheme({
  ...shared,
  palette: {
    mode: 'light',
    primary: { main: '#006B3C', contrastText: '#FFFFFF' },
    secondary: { main: '#00696D', contrastText: '#FFFFFF' },
    error: { main: '#BA1A1A' },
    background: { default: '#FFFBFF', paper: '#FFFBFF' },
    text: { primary: '#1A1C19', secondary: '#3F4239' },
    divider: '#D0D0C8',
  },
})

export const darkTheme = createTheme({
  ...shared,
  palette: {
    mode: 'dark',
    primary: { main: '#7DD9A4', contrastText: '#003D22' },
    secondary: { main: '#4FD8DC', contrastText: '#003739' },
    error: { main: '#FFB4AB', contrastText: '#690005' },
    background: { default: '#1A1C19', paper: '#1A1C19' },
    text: { primary: '#E3E3E0', secondary: '#C4C6BC' },
    divider: '#3F4239',
  },
})
