export interface MonitorHealth {
  overallStatus: 'UP' | 'DEGRADED' | 'DOWN';
  checkedAt: string;
  services: ServiceHealth[];
}

export interface ServiceHealth {
  name: string;
  group: 'PM' | 'MVP';
  type: string;
  url: string;
  status: 'UP' | 'DOWN' | 'UNKNOWN';
  responseTimeMs: number;
  lastChecked: string;
  errorMessage?: string;
}
