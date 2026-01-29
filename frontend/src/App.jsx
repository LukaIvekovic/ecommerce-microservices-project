import { useState } from 'react'
import './App.css'
import ConfigConsole from './ConfigConsole'

function App() {
    const [activeTab, setActiveTab] = useState('order')
    const [loading, setLoading] = useState(false)
    const [response, setResponse] = useState(null)
    const [error, setError] = useState(null)
    const [pattern, setPattern] = useState(null)

    const placeOrder = async (type) => {
        setLoading(true)
        setError(null)
        setResponse(null)
        setPattern(type)

        const orderData = {
            customerName: "John Doe",
            customerEmail: "john.doe@example.com",
            shippingAddress: "123 Main Street, New York, NY 10001",
            paymentMethod: "CREDIT_CARD",
            paymentProvider: "FINA",
            cardLastFourDigits: "4242",
            carrier: "GLS",
            orderItems: [
                { productId: 1, quantity: 2 },
                { productId: 2, quantity: 1 }
            ]
        }

        const endpoint = type === 'Saga'
            ? 'http://localhost:8080/api/gateway/place-order-saga'
            : 'http://localhost:8080/api/gateway/place-order-2pc'

        try {
            const res = await fetch(endpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(orderData)
            })

            const data = await res.json()

            // uvijek spremi response da prikaže metrike i greške
            setResponse(data)

            if (!data.success) {
                // prikazuje samo ključnu poruku
                const msg = data.errorDetails || data.message || `Failed to place order with ${type}`
                setError(msg)
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

            <div className="tabs">
                <button
                    className={`tab ${activeTab === 'order' ? 'active' : ''}`}
                    onClick={() => setActiveTab('order')}
                >
                    📦 Place Order
                </button>
                <button
                    className={`tab ${activeTab === 'config' ? 'active' : ''}`}
                    onClick={() => setActiveTab('config')}
                >
                    ⚙️ Configuration
                </button>
            </div>

            {activeTab === 'order' && (
                <>
                    <div className="card">
                        <h3>Choose Transaction Pattern:</h3>
                        <div className="button-group">
                            <button
                                onClick={() => placeOrder('Saga')}
                                disabled={loading}
                                className="order-button saga-button"
                            >
                                {loading && pattern === 'Saga'
                                    ? '⏳ Processing...'
                                    : '📦 Place Order with Saga'}
                            </button>

                            <button
                                onClick={() => placeOrder('2PC')}
                                disabled={loading}
                                className="order-button tpc-button"
                            >
                                {loading && pattern === '2PC'
                                    ? '⏳ Processing...'
                                    : '🔒 Place Order with 2PC'}
                            </button>
                        </div>

                        <p className="info">
                            Sample order: 2x Laptop + 1x Smartphone
                        </p>
                        <p className="info-small">
                            <strong>Saga:</strong> Eventual consistency with compensations<br />
                            <strong>2PC:</strong> Strong consistency with atomic commits
                        </p>
                    </div>

                    {/* PRIKAZ ERRORA */}
                    {error && (
                        <div className="error-box">
                            <h3>❌ Order Failed</h3>
                            <p>{error}</p>
                        </div>
                    )}

                    {/* RESPONSE – metrike i podaci */}
                    {response && (
                        <div className={response.success ? "success-box" : "error-box"}>
                            <h3>
                                {response.success
                                    ? "✅ Order Completed"
                                    : "⚠️ Order Failed – Metrics Available"}
                            </h3>

                            <div className="response-details">
                                {response.orderId && (
                                    <>
                                        <p><strong>Order ID:</strong> {response.orderId}</p>
                                        <p><strong>Order Status:</strong> {response.orderStatus}</p>
                                        <p><strong>Total Amount:</strong> ${response.totalAmount?.toFixed(2)}</p>
                                    </>
                                )}

                                {response.paymentId && (
                                    <>
                                        <p><strong>Payment ID:</strong> {response.paymentId}</p>
                                        <p><strong>Transaction ID:</strong> {response.transactionId}</p>
                                        <p><strong>Payment Status:</strong> {response.paymentStatus}</p>
                                    </>
                                )}

                                {response.shipmentId && (
                                    <>
                                        <p><strong>Shipment ID:</strong> {response.shipmentId}</p>
                                        <p><strong>Tracking Number:</strong> {response.trackingNumber}</p>
                                        <p><strong>Shipment Status:</strong> {response.shipmentStatus}</p>
                                    </>
                                )}

                                {!response.success && response.errorDetails && (
                                    <p><strong>Error Details:</strong> {response.errorDetails}</p>
                                )}

                                <h4>📊 Metrics</h4>
                                <p><strong>Order Service Latency:</strong> {response.orderLatency} ms</p>
                                <p><strong>Payment Service Latency:</strong> {response.paymentLatency} ms</p>
                                <p><strong>Shipping Service Latency:</strong> {response.shippingLatency} ms</p>
                                {response.prepareLatency !== undefined && (
                                    <p><strong>Prepare Phase Latency:</strong> {response.prepareLatency} ms</p>
                                )}
                                {response.commitLatency !== undefined && (
                                    <p><strong>Commit Phase Latency:</strong> {response.commitLatency} ms</p>
                                )}
                                <p><strong>Abort Phase Latency:</strong> {response.abortLatency} ms</p>
                                <p><strong>Total Order Latency:</strong> {response.totalLatency} ms</p>
                                <p><strong>Rollback / Compensations:</strong> {response.compensations}</p>
                            </div>
                        </div>
                    )}
                </>
            )}

            {activeTab === 'config' && <ConfigConsole />}
        </div>
    )
}

export default App
