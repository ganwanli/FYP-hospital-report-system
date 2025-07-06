import { http } from '@/utils/request';

// 节点管理
export async function searchNodes(params: {
  keyword?: string;
  nodeType?: string;
  systemSource?: string;
}) {
  return http.get('/api/lineage/nodes', { params });
}

export async function createOrUpdateNode(node: any) {
  return http.post('/api/lineage/nodes', node);
}

export async function getNodeDetail(nodeId: string) {
  return http.get(`/api/lineage/nodes/${nodeId}`);
}

export async function updateNodePosition(nodeId: string, positionX: number, positionY: number) {
  return http.put(`/api/lineage/nodes/${nodeId}/position`, null, {
    params: { positionX, positionY }
  });
}

export async function getNodeStatistics() {
  return http.get('/api/lineage/nodes/statistics');
}

export async function getNodeCategoryStats() {
  return http.get('/api/lineage/nodes/categories');
}

export async function getCriticalNodes(limit: number = 20) {
  return http.get('/api/lineage/nodes/critical', { params: { limit } });
}

export async function checkNodeHealth(nodeId: string) {
  return http.get(`/api/lineage/nodes/${nodeId}/health`);
}

export async function importNodes(nodes: any[]) {
  return http.post('/api/lineage/nodes/import', nodes);
}

export async function discoverNodes(systemSource: string, config: any) {
  return http.post('/api/lineage/nodes/discover', config, {
    params: { systemSource }
  });
}

export async function getNodeHierarchy(nodeId: string) {
  return http.get(`/api/lineage/nodes/${nodeId}/hierarchy`);
}

export async function syncNodeMetadata(nodeId: string, metadata: any) {
  return http.put(`/api/lineage/nodes/${nodeId}/metadata`, metadata);
}

// 血缘关系管理
export async function getNodeLineage(nodeId: string, params?: {
  maxDepth?: number;
  direction?: string;
}) {
  return http.get(`/api/lineage/${nodeId}`, { 
    params: { maxDepth: 5, direction: 'ALL', ...params }
  });
}

export async function getLineageGraph(nodeId: string, params?: {
  maxDepth?: number;
  direction?: string;
}) {
  return http.get(`/api/lineage/${nodeId}/graph`, { 
    params: { maxDepth: 5, direction: 'ALL', ...params }
  });
}

export async function buildLineageRelation(params: {
  sourceNodeId: string;
  targetNodeId: string;
  relationType: string;
  transformRule?: string;
  metadata?: any;
}) {
  const { metadata, ...queryParams } = params;
  return http.post('/api/lineage/relations', metadata || {}, { params: queryParams });
}

export async function performImpactAnalysis(nodeId: string, params: {
  changeType: string;
  analysisDepth?: number;
}) {
  return http.post(`/api/lineage/${nodeId}/impact-analysis`, null, {
    params: { analysisDepth: 3, ...params }
  });
}

export async function searchLineage(params: {
  keyword: string;
  nodeType?: string;
  relationType?: string;
}) {
  return http.get('/api/lineage/search', { params });
}

export async function getLineageStatistics() {
  return http.get('/api/lineage/statistics');
}

export async function verifyLineageRelation(lineageId: number, params: {
  verificationStatus: string;
  verificationComment?: string;
}) {
  return http.put(`/api/lineage/relations/${lineageId}/verify`, null, { params });
}

export async function discoverLineageRelations(systemSource: string, discoveryMethod: string) {
  return http.post('/api/lineage/discover', null, {
    params: { systemSource, discoveryMethod }
  });
}

export async function getDataFlowAnalysis(nodeId: string) {
  return http.get(`/api/lineage/${nodeId}/data-flow`);
}

export async function getLineagePath(sourceNodeId: string, targetNodeId: string) {
  return http.get('/api/lineage/path', {
    params: { sourceNodeId, targetNodeId }
  });
}

export async function importLineageRelations(relations: any[]) {
  return http.post('/api/lineage/relations/import', relations);
}

export async function exportLineageRelations(nodeId: string, format: string = 'JSON') {
  return http.get(`/api/lineage/${nodeId}/export`, {
    params: { format },
    responseType: 'blob',
  });
}

export async function detectCircularDependencies() {
  return http.get('/api/lineage/circular-dependencies');
}

export async function getOrphanNodes() {
  return http.get('/api/lineage/orphan-nodes');
}

export async function performLineageHealthCheck() {
  return http.get('/api/lineage/health-check');
}