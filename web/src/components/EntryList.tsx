import type { Category, Entry } from '../types.ts'

interface Props {
  entries: Entry[]
  search: string
  category: Category
}

function matchesSearch(entry: Entry, needle: string): boolean {
  const lower = needle.toLowerCase()
  return (
    entry.className.toLowerCase().includes(lower) ||
    entry.name.toLowerCase().includes(lower) ||
    entry.javadoc.toLowerCase().includes(lower)
  )
}

function shortClassName(className: string): string {
  const parts = className.split('.')
  return parts[parts.length - 1]
}

function groupByClass(entries: Entry[]): Map<string, Entry[]> {
  const map = new Map<string, Entry[]>()
  for (const entry of entries) {
    const group = map.get(entry.className) ?? []
    group.push(entry)
    map.set(entry.className, group)
  }
  return map
}

function EmptyState({ search, category }: { search: string; category: Category }) {
  return (
    <p className="text-[var(--text)] text-sm py-8 text-center">
      {search ? `No results for "${search}"` : `No ${category} found.`}
    </p>
  )
}

function EventRow({ entry }: { entry: Entry }) {
  return (
    <div className="flex flex-col gap-1 py-2.5 border-t border-[var(--border)] first:border-t-0">
      <div className="flex items-baseline gap-2 flex-wrap">
        <code className="text-[var(--accent)] font-medium text-sm">{entry.name}</code>
        {entry.type && (
          <code className="text-[var(--text)] text-xs">{entry.type}</code>
        )}
      </div>
      {entry.javadoc && (
        <p className="text-[var(--text)] text-sm leading-relaxed whitespace-pre-wrap">{entry.javadoc.trim()}</p>
      )}
    </div>
  )
}

function EventGroup({ className, entries }: { className: string; entries: Entry[] }) {
  return (
    <div className="border border-[var(--border)] rounded-lg overflow-hidden">
      <div className="px-4 py-2 bg-[var(--code-bg)] border-b border-[var(--border)]">
        <code className="text-[var(--text-h)] text-sm font-medium">{shortClassName(className)}</code>
        <span className="text-[var(--text)] text-xs ml-2">{className}</span>
      </div>
      <div className="px-4 divide-y divide-[var(--border)]">
        {entries.map((entry, i) => (
          <EventRow key={i} entry={entry} />
        ))}
      </div>
    </div>
  )
}

function EntryCard({ entry }: { entry: Entry }) {
  return (
    <div className="border border-[var(--border)] rounded-lg px-4 py-3 bg-[var(--bg)] hover:border-[var(--accent-border)] transition-colors">
      <div className="flex items-baseline gap-2 flex-wrap">
        {entry.name && (
          <code className="text-[var(--accent)] font-medium">{entry.name}</code>
        )}
        {entry.type && (
          <code className="text-[var(--text)] text-xs">{entry.type}</code>
        )}
        <span className="text-[var(--text)] text-xs ml-auto">{shortClassName(entry.className)}</span>
      </div>
      {entry.javadoc && (
        <p className="text-[var(--text)] text-sm mt-1 leading-relaxed whitespace-pre-wrap">{entry.javadoc.trim()}</p>
      )}
    </div>
  )
}

export default function EntryList({ entries, search, category }: Props) {
  const needle = search.trim()
  const filtered = needle ? entries.filter(e => matchesSearch(e, needle)) : entries

  if (filtered.length === 0) {
    return <EmptyState search={search} category={category} />
  }

  if (category === 'events') {
    const groups = groupByClass(filtered)
    return (
      <div className="flex flex-col gap-3">
        {[...groups.entries()].map(([className, groupEntries]) => (
          <EventGroup key={className} className={className} entries={groupEntries} />
        ))}
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-2">
      {filtered.map((entry, i) => (
        <EntryCard key={i} entry={entry} />
      ))}
    </div>
  )
}
