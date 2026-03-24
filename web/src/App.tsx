import { useState, useEffect } from 'react'
import type { Category, VersionData } from './types.ts'
import VersionPicker from './components/VersionPicker.tsx'
import CategoryTabs from './components/CategoryTabs.tsx'
import SearchBox from './components/SearchBox.tsx'
import EntryList from './components/EntryList.tsx'

export default function App() {
  const [versions, setVersions] = useState<string[]>([])
  const [selectedVersion, setSelectedVersion] = useState<string | null>(null)
  const [data, setData] = useState<VersionData | null>(null)
  const [activeTab, setActiveTab] = useState<Category>('events')
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const base = import.meta.env.BASE_URL.replace(/\/$/, '')

  useEffect(() => {
    fetch(`${base}/metadata/versions.json`)
      .then(r => r.json())
      .then((list: string[]) => {
        setVersions(list)
        if (list.length > 0) setSelectedVersion(list[0])
      })
      .catch(() => setError('Could not load versions.json'))
  }, [])

  useEffect(() => {
    if (!selectedVersion) return
    setLoading(true)
    setError(null)
    setData(null)
    fetch(`${base}/metadata/${selectedVersion}.json`)
      .then(r => r.json())
      .then((d: VersionData) => setData(d))
      .catch(() => setError(`Could not load ${selectedVersion}.json`))
      .finally(() => setLoading(false))
  }, [selectedVersion])

  const entries = data ? data[activeTab] : []

  const counts = data
    ? { events: data.events.length, registries: data.registries.length, callbacks: data.callbacks.length }
    : { events: 0, registries: 0, callbacks: 0 }

  return (
    <div className="flex flex-col min-h-screen">
      <header className="border-b border-[var(--border)] px-6 py-4 flex items-center justify-between gap-4 flex-wrap">
        <h1 className="text-2xl font-medium tracking-tight text-[var(--text-h)] m-0">Twill</h1>
        <VersionPicker versions={versions} selected={selectedVersion} onChange={v => { setSelectedVersion(v); setSearch('') }} />
      </header>

      <div className="flex flex-col gap-4 px-6 py-4 flex-1">
        <div className="flex items-center justify-between gap-4 flex-wrap">
          <CategoryTabs active={activeTab} counts={counts} onChange={tab => { setActiveTab(tab); setSearch('') }} />
          <SearchBox value={search} onChange={setSearch} />
        </div>

        {error && (
          <p className="text-red-500 text-sm">{error}</p>
        )}

        {loading && (
          <p className="text-[var(--text)] text-sm">Loading…</p>
        )}

        {!loading && !error && (
          <EntryList entries={entries} search={search} category={activeTab} />
        )}
      </div>
    </div>
  )
}
