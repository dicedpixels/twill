export type Category = 'events' | 'registries' | 'callbacks'

export interface Entry {
  className: string
  name: string
  type: string
  javadoc: string
}

export interface VersionData {
  version: string
  events: Entry[]
  registries: Entry[]
  callbacks: Entry[]
}
