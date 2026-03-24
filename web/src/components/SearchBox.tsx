interface Props {
  value: string
  onChange: (value: string) => void
}

export default function SearchBox({ value, onChange }: Props) {
  return (
    <input
      type="search"
      placeholder="Search…"
      value={value}
      onChange={e => onChange(e.target.value)}
      className="bg-[var(--code-bg)] border border-[var(--border)] text-[var(--text-h)] text-sm rounded px-3 py-1.5 w-56 placeholder:text-[var(--text)] focus:outline-none focus:border-[var(--accent)]"
    />
  )
}
