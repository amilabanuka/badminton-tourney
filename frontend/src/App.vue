<template>
  <div id="app">
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
      <div class="container">
        <router-link class="navbar-brand" to="/">Badminton Manager</router-link>
        <button
          class="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#mainNav"
          aria-controls="mainNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNav">
          <div class="navbar-nav ms-auto">
            <router-link class="nav-link" to="/">Home</router-link>
            <router-link class="nav-link" to="/about">About</router-link>
          </div>
        </div>
      </div>
    </nav>
    <main class="container py-4">
      <router-view />
    </main>
  </div>
</template>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.navbar {
  margin-bottom: 30px;
}

.nav-link {
  font-weight: bold;
  color: #2c3e50;
}

.nav-link.router-link-exact-active {
  color: #42b983;
}
</style>
