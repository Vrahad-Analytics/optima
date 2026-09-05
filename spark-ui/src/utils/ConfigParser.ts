// Utility to parse the spark.optima.alert.disabled config
export function parseAlertDisabledConfig(config: string | undefined): Set<string> {
  if (!config) return new Set();
  return new Set(config.split(',').map(x => x.trim()).filter(Boolean));
}
