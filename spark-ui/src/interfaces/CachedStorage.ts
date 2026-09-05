export interface OptimaExecutorStorageInfo {
    memoryUsed: number;
    memoryRemaining: number;
    memoryUsagePercentage: number;
}

export interface RddStorageInfo {
    rddId: number;
    memoryUsed: number;
    diskUsed: number;
    numOfPartitions: number;
    storageLevel: string;
    maxMemoryExecutorInfo: OptimaExecutorStorageInfo | undefined;
}

export interface CachedStorage {
    [stageId: string]: RddStorageInfo[];
}
