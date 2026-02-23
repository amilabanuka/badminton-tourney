<template>
  <div id="app">
    <NavBar />
    <main class="container py-4">
      <router-view />
    </main>
  </div>
</template>

<script>
import NavBar from './components/layout/NavBar.vue'

export default {
  name: 'App',
  components: {
    NavBar
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
