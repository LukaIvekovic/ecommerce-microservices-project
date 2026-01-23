import { useState } from 'react'
import './App.css'

function App() {
  const [loading, setLoading] = useState(false)
  const [response, setResponse] = useState(null)
  const [error, setError] = useState(null)

  const placeOrderWithSaga = async () => {
    setLoading(true)
    setError(null)
    setResponse(null)

    const orderData = {
      customerName: "John Doe",
      customerEmail: "john.doe@example.com",
      shippingAddress: "123 Main Street, New York, NY 10001",
      paymentMethod: "CREDIT_CARD",
      paymentProvider: "FINA",
      cardLastFourDigits: "4242",
      carrier: "GLS",
      orderItems: [
        {
          productId: 1,
          quantity: 2
        },
        {
          productId: 2,
          quantity: 1
        }
      ]
    }

    try {
      const res = await fetch('http://localhost:8080/api/gateway/place-order-saga', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(orderData)
      })

      const data = await res.json()

      if (data.success) {
        setResponse(data)
      } else {
        setError(data.message || 'Failed to place order with Saga')
      }
    } catch (err) {
      setError('Error: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const placeOrderWith2PC = async () => {
    setLoading(true)
    setError(null)
    setResponse(null)

    const orderData = {
      customerName: "John Doe",
      customerEmail: "john.doe@example.com",
      shippingAddress: "123 Main Street, New York, NY 10001",
      paymentMethod: "CREDIT_CARD",
      paymentProvider: "FINA",
      cardLastFourDigits: "4242",
      carrier: "GLS",
      orderItems: [
        {
          productId: 1,
          quantity: 2
        },
        {
          productId: 2,
          quantity: 1
        }
      ]
    }

    try {
      const res = await fetch('http://localhost:8080/api/gateway/place-order-2pc', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(orderData)
      })

      const data = await res.json()

      if (data.success) {
        setResponse(data)
      } else {
        setError(data.message || 'Failed to place order with 2PC')
      }
    } catch (err) {
      setError('Error: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app">
      <h1>🛒 E-Commerce Order System</h1>
      <p>Distributed Transaction Patterns Demo</p>

      <div className="card">
        <h3>Choose Transaction Pattern:</h3>

        <div className="button-group">
          <button
            onClick={placeOrderWithSaga}
            disabled={loading}
            className="order-button saga-button"
          >
            {loading ? '⏳ Processing...' : '📦 Place Order with Saga'}
          </button>

          <button
            onClick={placeOrderWith2PC}
            disabled={loading}
            className="order-button tpc-button"
          >
            {loading ? '⏳ Processing...' : '🔒 Place Order with 2PC'}
          </button>
        </div>

        <p className="info">
          Sample order: 2x Laptop + 1x Smartphone
        </p>
        <p className="info-small">
          <strong>Saga:</strong> Eventual consistency with compensations<br/>
          <strong>2PC:</strong> Strong consistency with atomic commits
        </p>
      </div>

      {error && (
        <div className="error-box">
          <h3>❌ Error</h3>
          <p>{error}</p>
        </div>
      )}

      {response && (
        <div className="success-box">
          <h3>✅ Order Placed Successfully!</h3>
          <div className="response-details">
            <p><strong>Order ID:</strong> {response.orderId}</p>
            <p><strong>Order Status:</strong> {response.orderStatus}</p>
            <p><strong>Total Amount:</strong> ${response.totalAmount?.toFixed(2)}</p>
            <p><strong>Payment ID:</strong> {response.paymentId}</p>
            <p><strong>Transaction ID:</strong> {response.transactionId}</p>
            <p><strong>Payment Status:</strong> {response.paymentStatus}</p>
            <p><strong>Shipment ID:</strong> {response.shipmentId}</p>
            <p><strong>Tracking Number:</strong> {response.trackingNumber}</p>
            <p><strong>Shipment Status:</strong> {response.shipmentStatus}</p>
          </div>
        </div>
      )}
    </div>
  )
}

export default App
