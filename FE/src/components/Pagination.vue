<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
}>();

const emit = defineEmits(['page-change']);

const pages = computed(() => {
  const range = [];
  const start = Math.max(0, props.currentPage - 2);
  const end = Math.min(props.totalPages - 1, props.currentPage + 2);
  for (let i = start; i <= end; i++) {
    range.push(i);
  }
  return range;
});

const changePage = (page: number) => {
  if (page >= 0 && page < props.totalPages && page !== props.currentPage) {
    emit('page-change', page);
  }
};
</script>

<template>
  <div class="pagination" v-if="totalPages > 0">
    <div class="info">
      Showing {{ currentPage * pageSize + 1 }} to {{ Math.min((currentPage + 1) * pageSize, totalElements) }} of {{ totalElements }}.
    </div>
    <div class="btns">
      <button class="btn-page" :disabled="currentPage === 0" @click="changePage(currentPage - 1)">Previous</button>
      
      <button 
        v-for="page in pages" 
        :key="page" 
        class="btn-page" 
        :class="{ active: page === currentPage }"
        @click="changePage(page)"
      >
        {{ page + 1 }}
      </button>
      
      <button class="btn-page" :disabled="currentPage === totalPages - 1" @click="changePage(currentPage + 1)">Next</button>
    </div>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-top: 1px solid var(--border-color);
  margin-top: 1rem;
}

.info {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.btns {
  display: flex;
  gap: 0.25rem;
}

.btn-page {
  padding: 0.4rem 0.8rem;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s;
}

.btn-page:hover:not(:disabled) {
  background: var(--bg-body);
  border-color: var(--color-primary);
}

.btn-page.active {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
