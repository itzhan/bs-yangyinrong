<template>
  <div id="app">
    <Navbar v-if="showNavbar" />
    <router-view />
    <Footer v-if="showNavbar" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'

const route = useRoute()
const showNavbar = computed(() => !route.meta.hideLayout)
</script>
