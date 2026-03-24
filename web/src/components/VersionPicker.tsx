interface Props {
  versions: string[]
  selected: string | null
  onChange: (version: string) => void
}

export default function VersionPicker({ versions, selected, onChange }: Props) {
  if (versions.length === 0) {
    return <span className="text-[var(--text)] text-sm">No versions available</span>
  }

  return (
    <select
      value={selected ?? ''}
      onChange={e => onChange(e.target.value)}
      className="bg-[var(--code-bg)] border border-[var(--border)] text-[var(--text-h)] text-sm rounded px-3 py-1.5 focus:outline-none focus:border-[var(--accent)]"
    >
      {versions.map(v => (
        <option key={v} value={v}>{v}</option>
      ))}
    </select>
  )
}
