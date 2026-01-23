import { useState, useEffect } from 'react'
import './ConfigConsole.css'

function ConfigConsole() {
  const [config, setConfig] = useState({
    fina: {
      finaAvailabilityEnabled: true,
      preAuthorizationEnabled: true,
    },
    carrier: {
      carrierAvailabilityEnabled: true,
      carrierCapacityEnabled: true,
    }
  })
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState(null)

  useEffect(() => {
    fetchConfig()
  }, [])

  const fetchConfig = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/gateway/config/status')
      const data = await res.json()
      setConfig(data)
    } catch (err) {
      console.error('Failed to fetch config:', err)
    }
  }

  const updateConfig = async (category, setting, enabled) => {
    setLoading(true)
    setMessage(null)

    try {
      const url = `http://localhost:8080/api/gateway/config/${category}/${setting}/${enabled}`
      const res = await fetch(url, { method: 'POST' })
      const data = await res.json()

      setMessage({
        type: 'success',
        text: data.message || 'Configuration updated'
      })

      await fetchConfig()
    } catch (err) {
      setMessage({
        type: 'error',
        text: 'Failed to update configuration'
      })
    } finally {
      setLoading(false)
    }
  }

  const handleFinaAvailability = (enabled) => {
    updateConfig('fina', 'availability', enabled)
  }

  const handleCarrierAvailability = (enabled) => {
    updateConfig('carrier', 'availability', enabled)
  }

  const handleCarrierCapacity = (enabled) => {
    updateConfig('carrier', 'capacity', enabled)
  }

  return (
    <div className="config-console">
      <h2>⚙️ Configuration Console</h2>
      <p className="config-subtitle">Control validation settings for testing</p>

      {message && (
        <div className={`config-message ${message.type}`}>
          {message.text}
        </div>
      )}

      <div className="config-sections">
        <div className="config-section">
          <h3>💳 Payment Service (FINA)</h3>

          <div className="config-item">
            <div className="config-info">
              <label>FINA Availability</label>
              <p>Controls if payment service is available</p>
            </div>
            <div className="config-toggle">
              <button
                className={config.fina?.finaAvailabilityEnabled ? 'active' : ''}
                onClick={() => handleFinaAvailability(true)}
                disabled={loading}
              >
                ✅ PASS
              </button>
              <button
                className={!config.fina?.finaAvailabilityEnabled ? 'active' : ''}
                onClick={() => handleFinaAvailability(false)}
                disabled={loading}
              >
                ❌ FAIL
              </button>
            </div>
          </div>
        </div>

        <div className="config-section">
          <h3>🚚 Shipping Service (Carrier)</h3>

          <div className="config-item">
            <div className="config-info">
              <label>Carrier Availability</label>
              <p>Controls if shipping carrier is available</p>
            </div>
            <div className="config-toggle">
              <button
                className={config.carrier?.carrierAvailabilityEnabled ? 'active' : ''}
                onClick={() => handleCarrierAvailability(true)}
                disabled={loading}
              >
                ✅ PASS
              </button>
              <button
                className={!config.carrier?.carrierAvailabilityEnabled ? 'active' : ''}
                onClick={() => handleCarrierAvailability(false)}
                disabled={loading}
              >
                ❌ FAIL
              </button>
            </div>
          </div>

          <div className="config-item">
            <div className="config-info">
              <label>Carrier Capacity</label>
              <p>Controls if carrier has available capacity</p>
            </div>
            <div className="config-toggle">
              <button
                className={config.carrier?.carrierCapacityEnabled ? 'active' : ''}
                onClick={() => handleCarrierCapacity(true)}
                disabled={loading}
              >
                ✅ PASS
              </button>
              <button
                className={!config.carrier?.carrierCapacityEnabled ? 'active' : ''}
                onClick={() => handleCarrierCapacity(false)}
                disabled={loading}
              >
                ❌ FAIL
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="config-help">
        <h4>💡 How to use:</h4>
        <ul>
          <li><strong>PASS:</strong> Validation will succeed - order can proceed</li>
          <li><strong>FAIL:</strong> Validation will fail - tests rollback scenarios</li>
          <li>Use FAIL settings to test Saga compensations and 2PC abort phases</li>
        </ul>
      </div>
    </div>
  )
}

export default ConfigConsole
