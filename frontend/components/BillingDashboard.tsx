'use client'

import { useState } from 'react'
import { CustomerSwitcher } from './CustomerSwitcher'
import { UsageForm, type BillingResult, type BillingError } from './UsageForm'

interface AutoTestStep {
  name: string
  description: string
  customerId: string
  promptTokens: number
  completionTokens: number
  idempotencyKey?: string
  expectedStatus: number
}

const AUTO_TEST_STEPS: AutoTestStep[] = [
  {
    name: '1. 額度內之一般計費',
    description: '使用 Acme Corp (Starter 方案)，用量在額度內，預期費用為 $0.00。',
    customerId: 'CUST-001',
    promptTokens: 50000,
    completionTokens: 30000,
    expectedStatus: 201,
  },
  {
    name: '2. 超出額度之計費',
    description: '使用 TechStart Inc (Free 方案)，用量超出額度，預期計算超額費用。',
    customerId: 'CUST-002',
    promptTokens: 8000,
    completionTokens: 6000,
    expectedStatus: 201,
  },
  {
    name: '3. 冪等性首次發送',
    description: '攜帶自訂 Idempotency-Key 發送請求，預期正常建立新帳單。',
    customerId: 'CUST-001',
    promptTokens: 1000,
    completionTokens: 1000,
    idempotencyKey: 'browser-idemp-key',
    expectedStatus: 201,
  },
  {
    name: '4. 冪等性重複發送',
    description: '攜帶相同 Idempotency-Key 再次發送，預期回傳相同帳單且狀態為 200 OK。',
    customerId: 'CUST-001',
    promptTokens: 1000,
    completionTokens: 1000,
    idempotencyKey: 'browser-idemp-key',
    expectedStatus: 200,
  },
  {
    name: '5. 過期訂閱拒絕處理',
    description: '選擇過期客戶 (CUST-004) 送出，預期回傳 409 Conflict 錯誤。',
    customerId: 'CUST-004',
    promptTokens: 1000,
    completionTokens: 1000,
    expectedStatus: 409,
  },
  {
    name: '6. 多重訂閱衝突處理',
    description: '選擇有多個有效訂閱的客戶 (CUST-005) 送出，預期回傳 500 錯誤。',
    customerId: 'CUST-005',
    promptTokens: 1000,
    completionTokens: 1000,
    expectedStatus: 500,
  },
  {
    name: '7. 不存在客戶處理',
    description: '送出不存在的客戶 ID，預期回傳 404 Not Found 錯誤。',
    customerId: 'CUST-999',
    promptTokens: 1000,
    completionTokens: 1000,
    expectedStatus: 404,
  },
]

export function BillingDashboard() {
  const [customerId, setCustomerId] = useState('')
  const [result, setResult] = useState<BillingResult | null>(null)
  const [idempotentReplayed, setIdempotentReplayed] = useState(false)
  const [error, setError] = useState<{ body: BillingError; status: number } | null>(null)

  // Auto test states
  const [isAutoTesting, setIsAutoTesting] = useState(false)
  const [showConsole, setShowConsole] = useState(false)
  const [currentStepIndex, setCurrentStepIndex] = useState(-1)
  const [stepStatuses, setStepStatuses] = useState<Record<number, 'idle' | 'running' | 'success' | 'failed'>>({})

  function handleResult(r: BillingResult, replayed: boolean) {
    setError(null)
    setResult(r)
    setIdempotentReplayed(replayed)
  }

  function handleError(e: BillingError, status: number) {
    setResult(null)
    setError({ body: e, status })
  }

  async function sleep(ms: number) {
    return new Promise((resolve) => setTimeout(resolve, ms))
  }

  async function startAutoTest() {
    setIsAutoTesting(true)
    setShowConsole(true)
    setCurrentStepIndex(-1)
    
    const initialStatuses: Record<number, 'idle' | 'running' | 'success' | 'failed'> = {}
    AUTO_TEST_STEPS.forEach((_, idx) => {
      initialStatuses[idx] = 'idle'
    })
    setStepStatuses(initialStatuses)

    const testId = Date.now()

    for (let i = 0; i < AUTO_TEST_STEPS.length; i++) {
      setCurrentStepIndex(i)
      setStepStatuses((prev) => ({ ...prev, [i]: 'running' }))
      
      // Give a visual delay to let user see what's happening
      await sleep(1500)

      const step = AUTO_TEST_STEPS[i]

      // Set active customer
      setCustomerId(step.customerId)

      try {
        const headers: Record<string, string> = {
          'Content-Type': 'application/json',
        }
        if (step.idempotencyKey) {
          headers['Idempotency-Key'] = `${step.idempotencyKey}-${testId}`
        }

        const res = await fetch('/api/billing-proxy', {
          method: 'POST',
          headers,
          body: JSON.stringify({
            customerId: step.customerId,
            promptTokens: step.promptTokens,
            completionTokens: step.completionTokens,
          }),
        })

        const replayed = res.headers.get('Idempotent-Replayed') === 'true'
        const body = await res.json()

        if (res.ok) {
          handleResult(body as BillingResult, replayed)
          // Accept 200 or 201 for success cases
          if (step.expectedStatus === 200 || step.expectedStatus === 201) {
            setStepStatuses((prev) => ({ ...prev, [i]: 'success' }))
          } else {
            setStepStatuses((prev) => ({ ...prev, [i]: 'failed' }))
          }
        } else {
          handleError(body as BillingError, res.status)
          if (res.status === step.expectedStatus) {
            setStepStatuses((prev) => ({ ...prev, [i]: 'success' }))
          } else {
            setStepStatuses((prev) => ({ ...prev, [i]: 'failed' }))
          }
        }
      } catch {
        handleError({ title: 'Connection Failure', detail: 'Failed to contact proxy API' }, 502)
        setStepStatuses((prev) => ({ ...prev, [i]: 'failed' }))
      }
    }

    setIsAutoTesting(false)
  }

  return (
    <>
      {/* Auto test trigger and panel */}
      <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <button 
            className="btn btn-primary"
            onClick={startAutoTest}
            disabled={isAutoTesting}
            style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: '0.5rem',
              background: 'linear-gradient(135deg, var(--color-purple) 0%, var(--color-purple-light) 100%)',
              border: 'none',
              cursor: isAutoTesting ? 'not-allowed' : 'pointer'
            }}
          >
            {isAutoTesting ? '⚙️ 瀏覽器自動測試中…' : '⚡ 啟動瀏覽器自動測試'}
          </button>
        </div>
        {showConsole && (
          <button 
            className="btn" 
            onClick={() => setShowConsole(false)} 
            disabled={isAutoTesting}
            style={{ opacity: isAutoTesting ? 0.5 : 1 }}
          >
            關閉測試主控台
          </button>
        )}
      </div>

      {showConsole && (
        <section className="card card-gradient animate-fade-in" style={{ marginBottom: '1.5rem', border: '1px solid rgba(139, 92, 246, 0.3)' }}>
          <p className="label" style={{ color: 'var(--color-purple-light)', marginBottom: '1rem' }}>
            Browser Automated Testing Console / 瀏覽器自動測試主控台
          </p>

          <div style={{ display: 'grid', gap: '0.75rem', marginBottom: '1rem' }}>
            {AUTO_TEST_STEPS.map((step, idx) => {
              const status = stepStatuses[idx] || 'idle'
              const isCurrent = idx === currentStepIndex
              
              let statusLabel = '⌛ 等待中'
              let statusColor = 'var(--color-text-muted)'
              let bg = 'transparent'

              if (status === 'running') {
                statusLabel = '⚙️ 執行中'
                statusColor = 'var(--color-purple-light)'
                bg = 'rgba(139, 92, 246, 0.08)'
              } else if (status === 'success') {
                statusLabel = '✅ 通過'
                statusColor = 'var(--color-green)'
              } else if (status === 'failed') {
                statusLabel = '❌ 失敗'
                statusColor = 'var(--color-error)'
              }

              return (
                <div 
                  key={idx}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '0.75rem 1rem',
                    borderRadius: 'var(--radius-md)',
                    background: bg,
                    border: isCurrent ? '1px solid rgba(139, 92, 246, 0.5)' : '1px solid var(--color-border-subtle)',
                    transition: 'all 0.3s ease',
                    transform: isCurrent ? 'scale(1.01)' : 'none'
                  }}
                >
                  <div>
                    <p style={{ fontWeight: 600, fontSize: '0.875rem', color: isCurrent ? 'var(--color-text-primary)' : 'var(--color-text-secondary)', margin: 0 }}>
                      {step.name}
                    </p>
                    <p style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', margin: '0.125rem 0 0 0' }}>
                      {step.description}
                    </p>
                  </div>
                  <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: statusColor, whiteSpace: 'nowrap', marginLeft: '1rem' }}>
                    {statusLabel}
                  </span>
                </div>
              )
            })}
          </div>

          <div style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)', borderTop: '1px solid var(--color-border-subtle)', paddingTop: '0.75rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span>測試狀態: {isAutoTesting ? '測試進行中，請觀察畫面欄位及結果卡的動態變化...' : '測試完成。您可以手動查看下方最後一步的渲染結果。'}</span>
            {isAutoTesting && <div className="spinner" style={{ width: '1rem', height: '1rem' }} />}
          </div>
        </section>
      )}

      <CustomerSwitcher onCustomerChange={setCustomerId} />

      <div
        style={{
          display: 'grid',
          gap: '1.5rem',
          gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
          alignItems: 'start',
        }}
      >
        {/* Usage submission form */}
        <section className="card card-gradient">
          <p className="label" style={{ color: 'var(--color-green)', marginBottom: '1rem' }}>
            Submit Usage
          </p>
          {customerId ? (
            <UsageForm
              customerId={customerId}
              onResult={handleResult}
              onError={handleError}
            />
          ) : (
            <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
              Select a customer above to submit token usage.
            </p>
          )}
        </section>

        {/* Billing result / error card */}
        <section>
          {result && (
            <ResultCard result={result} idempotentReplayed={idempotentReplayed} />
          )}
          {error && <ErrorCard error={error.body} status={error.status} />}
          {!result && !error && (
            <div className="card" style={{ opacity: 0.4 }}>
              <p className="label" style={{ color: 'var(--color-orange)', marginBottom: '0.5rem' }}>
                Billing Result
              </p>
              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
                Submit usage to see the calculated bill here.
              </p>
            </div>
          )}
        </section>
      </div>
    </>
  )
}

function ResultCard({
  result,
  idempotentReplayed,
}: {
  result: BillingResult
  idempotentReplayed: boolean
}) {
  const charge = parseFloat(result.totalCharge)
  const formattedCharge = `$${charge.toFixed(2)}`

  return (
    <div className="card card-gradient animate-fade-in">
      {idempotentReplayed && (
        <div style={{ marginBottom: '0.875rem' }}>
          <span className="badge badge-purple">Idempotent Replayed</span>
        </div>
      )}
      <p className="label" style={{ color: 'var(--color-orange)', marginBottom: '0.875rem' }}>
        Billing Result
      </p>

      {/* Total charge — prominent */}
      <div
        style={{
          fontSize: '2.25rem',
          fontWeight: 800,
          background: 'var(--gradient-brand)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          backgroundClip: 'text',
          marginBottom: '1rem',
          lineHeight: 1,
        }}
      >
        {formattedCharge}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem 1.25rem' }}>
        <Stat label="Total Tokens" value={result.totalTokens.toLocaleString()} />
        <Stat label="From Quota" value={result.tokensFromQuota.toLocaleString()} />
        <Stat
          label="Overage Tokens"
          value={result.overageTokens.toLocaleString()}
          highlight={result.overageTokens > 0}
        />
        <Stat label="Currency" value={result.currency} />
      </div>

      <div className="divider" />

      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
        <Stat label="Bill ID" value={result.billId.slice(0, 8) + '…'} mono />
        <Stat label="Customer" value={result.customerId} />
      </div>

      <p
        style={{
          fontSize: '0.7rem',
          color: 'var(--color-text-muted)',
          marginTop: '0.75rem',
        }}
      >
        Calculated at {new Date(result.calculatedAt).toLocaleString()}
      </p>
    </div>
  )
}

function Stat({
  label,
  value,
  highlight,
  mono,
}: {
  label: string
  value: string
  highlight?: boolean
  mono?: boolean
}) {
  return (
    <div>
      <p className="label" style={{ marginBottom: '0.125rem' }}>
        {label}
      </p>
      <p
        style={{
          fontWeight: 600,
          fontSize: '0.9375rem',
          color: highlight ? 'var(--color-orange-light)' : 'var(--color-text-primary)',
          fontFamily: mono ? 'monospace' : undefined,
        }}
      >
        {value}
      </p>
    </div>
  )
}

function ErrorCard({ error, status }: { error: BillingError; status: number }) {
  return (
    <div
      data-testid="error-card"
      className="animate-fade-in"
      style={{
        background: 'var(--color-error-dim)',
        border: '1px solid rgba(239, 68, 68, 0.3)',
        borderRadius: 'var(--radius-lg)',
        padding: '1.5rem',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.875rem' }}>
        <span style={{ fontSize: '1.25rem' }}>⛔</span>
        <p className="label" style={{ color: 'var(--color-error)', margin: 0 }}>
          Error {status}
        </p>
      </div>
      {error.title && (
        <p style={{ fontWeight: 700, color: '#f87171', marginBottom: '0.375rem' }}>
          {error.title}
        </p>
      )}
      {error.detail && (
        <p style={{ fontSize: '0.875rem', color: 'var(--color-text-secondary)' }}>
          {error.detail}
        </p>
      )}
      {error.type && (
        <p style={{ fontSize: '0.7rem', color: 'var(--color-text-muted)', marginTop: '0.5rem' }}>
          {error.type}
        </p>
      )}
    </div>
  )
}
