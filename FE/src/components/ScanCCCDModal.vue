<script setup lang="ts">
import { ref } from 'vue';

const show = ref(false);
const emit = defineEmits(['scanned']);

const open = () => show.value = true;
const close = () => show.value = false;

const simulateScan = () => {
  const mockData = {
    id: Math.floor(Math.random() * 1000000000000).toString(),
    fullName: 'NGUYỄN VĂN A',
    dob: '01/01/1990',
    address: 'Hà Nội, Việt Nam'
  };
  emit('scanned', mockData);
  close();
};

defineExpose({ open, close });
</script>

<template>
  <div v-if="show" class="modal-overlay" @click="close">
    <div class="modal-content" @click.stop>
      <h3>Scan CCCD (ID Card)</h3>
      <div class="scanner-placeholder">
        <i class="pi pi-id-card"></i>
        <p>Align ID card within the frame</p>
      </div>
      <div class="actions">
        <button class="btn-primary" @click="simulateScan">Simulate Successful Scan</button>
        <button class="btn-secondary" @click="close">Cancel</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  background: var(--bg-card);
  padding: 2rem;
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 400px;
  text-align: center;
}
.scanner-placeholder {
  height: 200px;
  border: 2px dashed var(--border-color);
  margin: 1.5rem 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
}
.scanner-placeholder i {
  font-size: 4rem;
  margin-bottom: 1rem;
}
.actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
</style>
