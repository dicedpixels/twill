import type { Category } from '../types.ts'

const TABS: { key: Category; label: string }[] = [
  { key: 'events', label: 'Events' },
  { key: 'registries', label: 'Registries' },
  { key: 'callbacks', label: 'Callbacks' },
]

interface Props {
  active: Category
  counts: Record<Category, number>
  onChange: (tab: Category) => void
}

export default function CategoryTabs({ active, counts, onChange }: Props) {
  return (
    <div className="flex gap-1">
      {TABS.map(({ key, label }) => {
        const isActive = key === active
        return (
          <button
            key={key}
            onClick={() => onChange(key)}
            className={[
              'flex items-center gap-1.5 px-3 py-1.5 rounded text-sm font-medium transition-colors',
              isActive
                ? 'bg-[var(--accent-bg)] text-[var(--accent)] border border-[var(--accent-border)]'
                : 'text-[var(--text)] hover:text-[var(--text-h)] border border-transparent',
            ].join(' ')}
          >
            {label}
            <span className={[
              'text-xs px-1.5 py-0.5 rounded-full',
              isActive ? 'bg-[var(--accent)] text-white' : 'bg-[var(--code-bg)] text-[var(--text)]',
            ].join(' ')}>
              {counts[key]}
            </span>
          </button>
        )
      })}
    </div>
  )
}
