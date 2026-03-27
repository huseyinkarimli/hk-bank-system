import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

// Browser extensions (e.g. autofill) may inject scripts that throw; not an app defect.
window.addEventListener(
  'error',
  (event) => {
    const msg = String(event.message ?? '')
    if (msg.includes('bootstrap-autofill')) {
      event.preventDefault()
      event.stopPropagation()
    }
  },
  true
)

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
