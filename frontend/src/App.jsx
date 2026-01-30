import { useState } from 'react'
import './App.css'
import ConfigConsole from './ConfigConsole'

function App() {
    const [activeTab, setActiveTab] = useState('order')
    const [loading, setLoading] = useState(false)
    const [response, setResponse] = useState(null)
    const [error, setError] = useState(null)
    const [pattern, setPattern] = useState(null)
    const [comparisonResults, setComparisonResults] = useState(null) // novi state
    const [batchResults, setBatchResults] = useState(null)
    const [individualRuns, setIndividualRuns] = useState([])



    const average = (arr) =>
        arr.length === 0 ? 0 : Math.round(arr.reduce((a, b) => a + b, 0) / arr.length)

    const sum = (arr, field) =>
        arr.reduce((s, x) => s + (x[field] || 0), 0)


    const calculateAverages = (results) => ({
        totalLatency: average(results.map(r => r.totalLatency)),
        orderLatency: average(results.map(r => r.orderLatency || 0)),
        paymentLatency: average(results.map(r => r.paymentLatency || 0)),
        shippingLatency: average(results.map(r => r.shippingLatency || 0)),
        prepareLatency: average(results.map(r => r.prepareLatency || 0)),
        commitLatency: average(results.map(r => r.commitLatency || 0)),
        abortLatency: average(results.map(r => r.abortLatency || 0)),
        compensations: average(results.map(r => r.compensations || 0)),
        successRate: Math.round(
            (results.filter(r => r.success).length / results.length) * 100
        )
    })
    const runBatchTest = async () => {
        setLoading(true)
        setError(null)

        setResponse(null)
        setComparisonResults(null)
        setBatchResults(null)
        setIndividualRuns([]) // ⬅️ novi state za pojedinačne runove

        const sagaRuns = []
        const tpcRuns = []

        for (let i = 0; i < 20; i++) {
            const result = await placeOrderForComparison('Saga')
            sagaRuns.push(result)
            setIndividualRuns(prev => [...prev, { pattern: 'Saga', run: i + 1, ...result }])
        }

        for (let i = 0; i < 20; i++) {
            const result = await placeOrderForComparison('2PC')
            tpcRuns.push(result)
            setIndividualRuns(prev => [...prev, { pattern: '2PC', run: i + 1, ...result }])
        }

        const avg = (arr, field) =>
            (arr.reduce((s, x) => s + (x[field] || 0), 0) / arr.length).toFixed(2)


        setBatchResults({
            saga: {
                totalBatchTime: sum(sagaRuns, 'totalLatency'),

                totalLatency: avg(sagaRuns, 'totalLatency'),
                orderLatency: avg(sagaRuns, 'orderLatency'),
                paymentLatency: avg(sagaRuns, 'paymentLatency'),
                shippingLatency: avg(sagaRuns, 'shippingLatency'),
                abortLatency: avg(sagaRuns, 'abortLatency'),
                compensations: (
                    sum(sagaRuns, 'compensations') / sagaRuns.length
                ).toFixed(2),
                successRate: Math.round(
                    sagaRuns.filter(x => x.success).length / sagaRuns.length * 100
                )
            },
            tpc: {
                totalBatchTime: sum(tpcRuns, 'totalLatency'),

                totalLatency: avg(tpcRuns, 'totalLatency'),
                orderLatency: avg(tpcRuns, 'orderLatency'),
                paymentLatency: avg(tpcRuns, 'paymentLatency'),
                shippingLatency: avg(tpcRuns, 'shippingLatency'),
                prepareLatency: avg(tpcRuns, 'prepareLatency'),
                commitLatency: avg(tpcRuns, 'commitLatency'),
                abortLatency: avg(tpcRuns, 'abortLatency'),
                successRate: Math.round(
                    tpcRuns.filter(x => x.success).length / tpcRuns.length * 100
                )
            }
        })


        setLoading(false)
    }




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

    const placeOrder = async (type) => {
        setLoading(true)
        setError(null)
        setResponse(null)
        setComparisonResults(null) // ukloni stare comparison rezultate
        setPattern(type)

        const endpoint = type === 'Saga'
            ? 'http://localhost:8080/api/gateway/place-order-saga'
            : 'http://localhost:8080/api/gateway/place-order-2pc'

        try {
            const startTime = performance.now()
            const res = await fetch(endpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(orderData)
            })
            const data = await res.json()
            const duration = Math.round(performance.now() - startTime)
            data.totalLatency = duration

            setResponse(data)

            if (!data.success) {
                const msg = data.errorDetails || data.message || `Failed to place order with ${type}`
                setError(msg)
            }
        } catch (err) {
            setError('Error: ' + err.message)
        } finally {
            setLoading(false)
        }
    }

    const comparePatterns = async () => {
        setLoading(true)
        setError(null)

        setResponse(null)
        setBatchResults(null)        // ⬅️ KLJUČNO
        setComparisonResults(null)

        const saga = await placeOrderForComparison('Saga')
        const tpc = await placeOrderForComparison('2PC')

        setComparisonResults({ saga, tpc })
        setLoading(false)
    }

    const placeOrderForComparison = async (type) => {
        const endpoint = type === 'Saga'
            ? 'http://localhost:8080/api/gateway/place-order-saga'
            : 'http://localhost:8080/api/gateway/place-order-2pc'

        try {
            const startTime = performance.now()
            const res = await fetch(endpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(orderData)
            })
            const data = await res.json()
            const duration = Math.round(performance.now() - startTime)
            data.totalLatency = duration
            data.pattern = type
            return data
        } catch (err) {
            return {
                pattern: type,
                success: false,
                message: err.message,
                totalLatency: 0
            }
        }
    }

    return (
        <div className="app" style={{ minWidth: '1300px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
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
                                    ? 'Processing...'
                                    : 'Place Order with Saga'}
                            </button>

                            <button
                                onClick={() => placeOrder('2PC')}
                                disabled={loading}
                                className="order-button tpc-button"
                            >
                                {loading && pattern === '2PC'
                                    ? 'Processing...'
                                    : 'Place Order with 2PC'}
                            </button>

                            <button
                                onClick={comparePatterns}
                                disabled={loading}
                                className="order-button compare-button"
                                style={{ backgroundColor: '#FFD54F', color: '#000' }}
                            >
                                {loading ? 'Comparing...' : 'Compare Saga vs 2PC'}
                            </button>
                            <button
                                onClick={runBatchTest}
                                disabled={loading}
                                className="order-button"
                                style={{ backgroundColor: '#D32F2F', color: '#fff' }}
                            >
                                {loading ? 'Running batch test...' : 'Run 20x Saga + 20x 2PC'}
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

                    {error && (
                        <div className="error-box">
                            <h3>❌ Order Failed</h3>
                            <p>{error}</p>
                        </div>
                    )}

                    {response && !comparisonResults && (
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
                                <p><strong>Abort Phase Latency:</strong> {response.abortLatency}</p>
                                <p><strong>Total Latency:</strong> {response.totalLatency} ms</p>
                                <p><strong>Rollback / Compensations:</strong> {response.compensations}</p>
                            </div>
                        </div>
                    )}
                    {/* COMPARISON – centrirano i 4 u redu */}
                    {comparisonResults && (
                        <div
                            style={{
                                display: 'flex',
                                justifyContent: 'center',
                                width: '100%',
                                marginTop: '1rem'
                            }}
                        >
                            <div style={{
                                display: 'flex',
                                gap: '1rem',
                                width: '1300px',
                                alignItems: 'flex-start',
                                flexWrap: 'nowrap'
                            }}>
                                {/* Lijevo general Saga */}
                                <div style={{
                                    flex: '0 0 250px',
                                    padding: '0.5rem',
                                    border: '1px solid #FBC02D',
                                    borderRadius: '6px',
                                    backgroundColor: '#FFF9C4'
                                }}>
                                    <h4>Saga – General Info</h4>
                                    <ul>
                                        <li>Eventual consistency</li>
                                        <li>Compensation transactions on failure</li>
                                        <li>No global locks</li>
                                        <li>High availability</li>
                                        <li>Lower latency under normal load</li>
                                        <li>Independent service transactions</li>
                                        <li>Resilient to partial failures</li>
                                        <li>Rollback handled by compensations</li>
                                    </ul>
                                    <p><strong>Last latency:</strong> {comparisonResults.saga.totalLatency} ms</p>


                                </div>

                                {/* Saga detalji */}
                                <div style={{
                                    flex: '1 1 400px',
                                    padding: '0.5rem',
                                    border: '1px solid #FBC02D',
                                    borderRadius: '6px',
                                    backgroundColor: '#FFF176',
                                    maxHeight: '600px',
                                    overflowY: 'auto'
                                }}>
                                    <h4>{comparisonResults.saga.pattern} {comparisonResults.saga.success ? '✅ Success' : '❌ Failed'}</h4>
                                    {/* svi podaci ostaju isti */}
                                    <p><strong>Total Latency:</strong> {comparisonResults.saga.totalLatency} ms</p>
                                    <p><strong>Order ID:</strong> {comparisonResults.saga.orderId || '-'}</p>
                                    <p><strong>Order Status:</strong> {comparisonResults.saga.orderStatus || '-'}</p>
                                    <p><strong>Total Amount:</strong> {comparisonResults.saga.totalAmount ? `$${comparisonResults.saga.totalAmount.toFixed(2)}` : '-'}</p>
                                    <p><strong>Payment ID:</strong> {comparisonResults.saga.paymentId || '-'}</p>
                                    <p><strong>Transaction ID:</strong> {comparisonResults.saga.transactionId || '-'}</p>
                                    <p><strong>Payment Status:</strong> {comparisonResults.saga.paymentStatus || '-'}</p>
                                    <p><strong>Shipment ID:</strong> {comparisonResults.saga.shipmentId || '-'}</p>
                                    <p><strong>Tracking Number:</strong> {comparisonResults.saga.trackingNumber || '-'}</p>
                                    <p><strong>Shipment Status:</strong> {comparisonResults.saga.shipmentStatus || '-'}</p>
                                    <p><strong>Order Service:</strong> {comparisonResults.saga.orderLatency || '-'} ms</p>
                                    <p><strong>Payment Service:</strong> {comparisonResults.saga.paymentLatency || '-'} ms</p>
                                    <p><strong>Shipping Service:</strong> {comparisonResults.saga.shippingLatency || '-'} ms</p>
                                    {comparisonResults.saga.prepareLatency !== undefined && <p><strong>Prepare Phase:</strong> {comparisonResults.saga.prepareLatency} ms</p>}
                                    {comparisonResults.saga.commitLatency !== undefined && <p><strong>Commit Phase:</strong> {comparisonResults.saga.commitLatency} ms</p>}
                                    <p><strong>Abort Phase Latency:</strong> {comparisonResults.saga.abortLatency}</p>
                                    <p><strong>Abort / Compensations:</strong> {comparisonResults.saga.compensations || 0}</p>
                                    {comparisonResults.saga.errorDetails && <p><strong>Error Details:</strong> {comparisonResults.saga.errorDetails}</p>}
                                    {comparisonResults.saga.message && <p><strong>Message:</strong> {comparisonResults.saga.message}</p>}
                                </div>

                                {/* 2PC detalji */}
                                <div style={{
                                    flex: '1 1 400px',
                                    padding: '0.5rem',
                                    border: '1px solid #FBC02D',
                                    borderRadius: '6px',
                                    backgroundColor: '#FFF176',
                                    maxHeight: '600px',
                                    overflowY: 'auto'
                                }}>
                                    <h4>{comparisonResults.tpc.pattern} {comparisonResults.tpc.success ? '✅ Success' : '❌ Failed'}</h4>
                                    {/* svi podaci ostaju isti */}
                                    <p><strong>Total Latency:</strong> {comparisonResults.tpc.totalLatency} ms</p>
                                    <p><strong>Order ID:</strong> {comparisonResults.tpc.orderId || '-'}</p>
                                    <p><strong>Order Status:</strong> {comparisonResults.tpc.orderStatus || '-'}</p>
                                    <p><strong>Total Amount:</strong> {comparisonResults.tpc.totalAmount ? `$${comparisonResults.tpc.totalAmount.toFixed(2)}` : '-'}</p>
                                    <p><strong>Payment ID:</strong> {comparisonResults.tpc.paymentId || '-'}</p>
                                    <p><strong>Transaction ID:</strong> {comparisonResults.tpc.transactionId || '-'}</p>
                                    <p><strong>Payment Status:</strong> {comparisonResults.tpc.paymentStatus || '-'}</p>
                                    <p><strong>Shipment ID:</strong> {comparisonResults.tpc.shipmentId || '-'}</p>
                                    <p><strong>Tracking Number:</strong> {comparisonResults.tpc.trackingNumber || '-'}</p>
                                    <p><strong>Shipment Status:</strong> {comparisonResults.tpc.shipmentStatus || '-'}</p>
                                    <p><strong>Order Service:</strong> {comparisonResults.tpc.orderLatency || '-'} ms</p>
                                    <p><strong>Payment Service:</strong> {comparisonResults.tpc.paymentLatency || '-'} ms</p>
                                    <p><strong>Shipping Service:</strong> {comparisonResults.tpc.shippingLatency || '-'} ms</p>
                                    {comparisonResults.tpc.prepareLatency !== undefined && <p><strong>Prepare Phase:</strong> {comparisonResults.tpc.prepareLatency} ms</p>}
                                    {comparisonResults.tpc.commitLatency !== undefined && <p><strong>Commit Phase:</strong> {comparisonResults.tpc.commitLatency} ms</p>}
                                    <p><strong>Abort Phase Latency:</strong> {comparisonResults.tpc.abortLatency}</p>
                                    <p>
                                        <strong>Abort / Compensations:</strong>{" "}
                                        {comparisonResults.tpc.compensations > 0
                                            ? "Global rollback"
                                            : "none"}
                                    </p>                                    {comparisonResults.tpc.errorDetails && <p><strong>Error Details:</strong> {comparisonResults.tpc.errorDetails}</p>}
                                    {comparisonResults.tpc.message && <p><strong>Message:</strong> {comparisonResults.tpc.message}</p>}
                                </div>

                                {/* Desno general 2PC */}
                                <div style={{
                                    flex: '0 0 250px',
                                    padding: '0.5rem',
                                    border: '1px solid #FBC02D',
                                    borderRadius: '6px',
                                    backgroundColor: '#FFF9C4'
                                }}>
                                    <h4>2PC – General Info</h4>
                                    <ul>
                                        <li>Strong consistency across all services</li>
                                        <li>Atomic commits: all or nothing</li>
                                        <li>Global locks can block other operations</li>
                                        <li>Lower availability than Saga</li>
                                        <li>Higher latency under load</li>
                                        <li>Strict transactional integrity</li>
                                        <li>Automatic rollback on failure</li>
                                        <li>Coordination overhead grows with participants</li>
                                    </ul>
                                    <p><strong>Last latency:</strong> {comparisonResults.tpc.totalLatency} ms</p>


                                </div>
                            </div>
                        </div>
                    )}
                    {batchResults && (
                        <div className="card" style={{ marginTop: '1rem', width: '900px' }}>
                            <h3>Batch Execution Time (20 requests)</h3>
                            <p>
                                <strong>Saga:</strong> {batchResults.saga.totalBatchTime} ms <br/>
                                <strong>2PC:</strong> {batchResults.tpc.totalBatchTime} ms
                            </p>
                            <h3>Batch Test Results (Average of 20 runs)</h3>

                            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                                <thead>
                                <tr>
                                    <th>Metric</th>
                                    <th>Saga</th>
                                    <th>2PC</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr><td>Total Latency (ms)</td><td>{batchResults.saga.totalLatency}</td><td>{batchResults.tpc.totalLatency}</td></tr>
                                <tr><td>Order Service (ms)</td><td>{batchResults.saga.orderLatency}</td><td>{batchResults.tpc.orderLatency}</td></tr>
                                <tr><td>Payment Service (ms)</td><td>{batchResults.saga.paymentLatency}</td><td>{batchResults.tpc.paymentLatency}</td></tr>
                                <tr><td>Shipping Service (ms)</td><td>{batchResults.saga.shippingLatency}</td><td>{batchResults.tpc.shippingLatency}</td></tr>
                                <tr><td>Prepare Phase (ms)</td><td>-</td><td>{batchResults.tpc.prepareLatency}</td></tr>
                                <tr><td>Commit Phase (ms)</td><td>-</td><td>{batchResults.tpc.commitLatency}</td></tr>
                                <tr><td>Abort Phase (ms)</td><td>-</td><td>{batchResults.tpc.abortLatency}</td></tr>
                                <tr>
                                    <td>Avg Compensations / Rollback</td>
                                    <td>{batchResults.saga.compensations}</td>
                                    <td>
                                        {batchResults.tpc.successRate < 100
                                            ? "Global rollback"
                                            : "none"}
                                    </td>
                                </tr>
                                <tr><td>Success Rate (%)</td><td>{batchResults.saga.successRate}%</td><td>{batchResults.tpc.successRate}%</td></tr>
                                </tbody>
                            </table>
                        </div>
                    )}
                    {individualRuns.length > 0 && (
                        <div className="card" style={{ marginTop: '1rem', width: '1200px' }}>
                            <h3>Individual Run Details (Saga & 2PC)</h3>
                            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                                <thead>
                                <tr>
                                    <th>Run #</th>
                                    <th>Pattern</th>
                                    <th>Total Latency</th>
                                    <th>Order</th>
                                    <th>Payment</th>
                                    <th>Shipping</th>
                                    <th>Prepare</th>
                                    <th>Commit</th>
                                    <th>Abort</th>
                                    <th>Compensations / Rollback</th>
                                    <th>Success</th>
                                </tr>
                                </thead>
                                <tbody>
                                {individualRuns.map((run, index) => (
                                    <tr key={index} style={{ backgroundColor: run.pattern === 'Saga' ? '#FFF9C4' : '#FFCDD2' }}>
                                        <td>{run.run}</td>
                                        <td>{run.pattern}</td>
                                        <td>{run.totalLatency || '-'}</td>
                                        <td>{run.orderLatency || '-'}</td>
                                        <td>{run.paymentLatency || '-'}</td>
                                        <td>{run.shippingLatency || '-'}</td>
                                        <td>{run.prepareLatency !== undefined ? run.prepareLatency : '-'}</td>
                                        <td>{run.commitLatency !== undefined ? run.commitLatency : '-'}</td>
                                        <td>{run.abortLatency || '-'}</td>
                                        <td>
                                            {run.pattern === 'Saga'
                                                ? run.compensations
                                                : run.success ? 'none' : 'Global rollback'}
                                        </td>
                                        <td>{run.success ? '✅' : '❌'}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                    )}

                </>
            )}

            {activeTab === 'config' && <ConfigConsole />}
        </div>
    )
}

export default App
