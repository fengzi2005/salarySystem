<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'
import * as THREE from 'three'

const router = useRouter()
const authStore = useAuthStore()
const loginForm = reactive({ username: '', password: '' })
const loading = ref(false)
const formRef = ref()
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await request.post('/api/auth/login', { username: loginForm.username, password: loginForm.password })
    if (res.data.code === 200) {
      const data = res.data.data
      authStore.setLoginInfo({ token: data.token, username: data.username, roleCode: data.roleCode, employeeId: data.employeeId, employeeName: data.employeeName || '', positionName: data.positionName || '' })
      ElMessage.success('登录成功'); router.push('/dashboard')
    } else { ElMessage.error(res.data.message || '登录失败') }
  } catch (err: any) { ElMessage.error(err.response?.data?.message || err.message || '登录失败') }
  finally { loading.value = false }
}

// Data Pillar 3D Model
let animId: number
onMounted(() => {
  const el = document.getElementById('model-canvas')!
  const w = el.clientWidth, h = el.clientHeight
  const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(w, h); renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  el.appendChild(renderer.domElement)

  const scene = new THREE.Scene()
  const camera = new THREE.PerspectiveCamera(40, w / h, 0.3, 20)
  camera.position.set(0.5, 0.2, 4); camera.lookAt(0, -0.2, 0)

  scene.add(new THREE.AmbientLight(0x1a3a6c, 0.5))
  scene.add(new THREE.PointLight(0x4a90d9, 3, 10)).position.set(2, 2, 3)
  scene.add(new THREE.PointLight(0x7eb8da, 2, 8)).position.set(-2, 1, -2)

  const group = new THREE.Group(); group.position.set(2.0, -0.9, 0); scene.add(group)
  const S = 0.22; const colors = [0x1a3a6c, 0x2c5fa1, 0x3b7dd8, 0x4a90d9, 0x5ba0f0, 0x7eb8da]
  const pillars: { m: THREE.Mesh, by: number, s: number, p: number, f: THREE.LineSegments }[] = []

  // Base
  const base = new THREE.Mesh(new THREE.CylinderGeometry(S*2, S*2.2, S*0.12, 48), new THREE.MeshPhongMaterial({ color: 0x1a3a6c, emissive: 0x0f2d54, emissiveIntensity: 0.4 }))
  base.position.y = -S*2.5; group.add(base)
  const baseRing = new THREE.Mesh(new THREE.TorusGeometry(S*2.1, S*0.03, 16, 50), new THREE.MeshPhongMaterial({ color: 0x4a90d9, emissive: 0x2c5fa1, emissiveIntensity: 0.7, transparent: true, opacity: 0.5 }))
  baseRing.rotation.x = Math.PI/2; baseRing.position.y = base.position.y+S*0.06; group.add(baseRing)

  // Grid
  for (let i=-3; i<=3; i++) {
    group.add(new THREE.Line(new THREE.BufferGeometry().setFromPoints([new THREE.Vector3(i*S*1.8, base.position.y+S*0.07, -3*S*1.8), new THREE.Vector3(i*S*1.8, base.position.y+S*0.07, 3*S*1.8)]), new THREE.LineBasicMaterial({ color: 0x4a90d9, transparent: true, opacity: 0.1 })))
    group.add(new THREE.Line(new THREE.BufferGeometry().setFromPoints([new THREE.Vector3(-3*S*1.8, base.position.y+S*0.07, i*S*1.8), new THREE.Vector3(3*S*1.8, base.position.y+S*0.07, i*S*1.8)]), new THREE.LineBasicMaterial({ color: 0x4a90d9, transparent: true, opacity: 0.1 })))
  }

  // 5x5 pillars
  for (let x=-2; x<=2; x++) {
    for (let z=-2; z<=2; z++) {
      if (x===0 && z===0) continue
      const h = S*(0.5+Math.random()*3)
      const geo = new THREE.BoxGeometry(S*0.5, h, S*0.5)
      const ci = Math.floor(Math.random()*colors.length)
      const mat = new THREE.MeshPhongMaterial({ color: colors[ci], emissive: colors[Math.min(ci+1,colors.length-1)], emissiveIntensity: 0.35, specular: 0x7eb8da, shininess: 50, transparent: true, opacity: 0.82 })
      const p = new THREE.Mesh(geo,mat)
      p.position.set(x*S*1.7, base.position.y+S*0.12+h/2, z*S*1.7); group.add(p)
      const f = new THREE.LineSegments(new THREE.EdgesGeometry(geo), new THREE.LineBasicMaterial({ color: 0x7eb8da, transparent: true, opacity: 0.18 }))
      f.position.copy(p.position); group.add(f)
      pillars.push({ m:p, by:p.position.y, s:0.2+Math.random()*0.4, p:Math.random()*Math.PI*2, f })
    }
  }

  // Center core
  const core = new THREE.Mesh(new THREE.OctahedronGeometry(S*0.28,0), new THREE.MeshPhongMaterial({ color: 0x7eb8da, emissive: 0x4a90d9, emissiveIntensity: 1, specular: 0xffffff, shininess: 100 }))
  core.position.y = base.position.y+S*0.22; group.add(core)
  const cRing = new THREE.Mesh(new THREE.TorusGeometry(S*0.5, S*0.015, 16, 40), new THREE.MeshPhongMaterial({ color: 0x7eb8da, emissive: 0x3b7dd8, emissiveIntensity: 0.9, transparent: true, opacity: 0.55 }))
  cRing.rotation.x = Math.PI/2; cRing.position.y = core.position.y; group.add(cRing)

  // Particles
  const dots = new THREE.Points(new THREE.BufferGeometry().setAttribute('position', new THREE.BufferAttribute(new Float32Array(Array.from({length:200},()=>(Math.random()-.5)*S*10)),3)), new THREE.PointsMaterial({ size: S*0.05, color: 0x7eb8da, transparent: true, opacity: 0.4, blending: THREE.AdditiveBlending }))
  group.add(dots)

  function animate() {
    animId = requestAnimationFrame(animate)
    const t = performance.now()*.001
    group.rotation.y += 0.002
    for (const p of pillars) {
      const sy = 1+Math.sin(t*p.s+p.p)*0.18
      p.m.scale.y = sy; p.m.position.y = p.by+(sy-1)*.5*(p.m.geometry as THREE.BoxGeometry).parameters.height
      p.f.scale.y = sy; p.f.position.y = p.m.position.y
      ;(p.m.material as THREE.MeshPhongMaterial).emissiveIntensity = 0.25+Math.sin(t*p.s*2+p.p)*0.25
    }
    dots.rotation.y += 0.0003
    renderer.render(scene, camera)
  }
  animate()

  window.addEventListener('resize', () => {
    const w2=el.clientWidth,h2=el.clientHeight
    camera.aspect=w2/h2; camera.updateProjectionMatrix(); renderer.setSize(w2,h2)
  })
})
onUnmounted(() => cancelAnimationFrame(animId))
</script>

<template>
  <div class="login-page">
    <!-- Left 3D model area -->
    <div class="model-area" id="model-canvas">
      <div class="model-text">
        <div class="model-title">工资管理系统</div>
        <div class="model-sub">Salary Management System</div>
      </div>
    </div>

    <!-- Animated gradient mesh background -->
    <div class="bg-mesh"></div>
    <!-- Floating orbs -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <!-- Centered login panel -->
    <div class="login-center">
      <div class="glass-panel">
        <div class="edge-scan"></div>
        <div class="edge-scan edge-scan-bottom"></div>

        <div class="panel-header">
          <h1 class="sys-name">用户登录</h1>
          <p class="sys-sub">Welcome Back</p>
        </div>

        <el-form ref="formRef" :model="loginForm" :rules="loginRules" size="large" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="用户名" :prefix-icon="User" clearable class="glass-input" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password clearable class="glass-input" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleLogin" class="login-btn" round>
              <span v-if="!loading">登 录</span><span v-else>验证中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="tips">
          <p class="tips-title">演示账号</p>
          <p>admin / 123456（管理员）</p>
          <p>sunqi / 123456（总经理）</p>
          <p>zhoumingyuan / 123456（普通员工）</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background: radial-gradient(ellipse at 40% 50%, rgba(26,58,108,.25) 0%, rgba(6,13,26,.85) 60%, #060d1a 100%);
  position: relative;
  overflow: hidden;
}

/* Left 3D model */
.model-area {
  flex: 0 0 44%;
  height: 100%;
  position: relative; z-index: 1;
  background: radial-gradient(ellipse at center, rgba(26,58,108,.3) 0%, rgba(6,13,26,.7) 100%);
}

.model-text {
  position: absolute; bottom: 31%; left: 0; right: 0;
  text-align: center; z-index: 2; pointer-events: none;
  transform: translateX(8%);
}
.model-title {
  font-size: 28px; font-weight: 700; color: #c8dff5;
  letter-spacing: 6px; text-shadow: 0 0 30px rgba(74,144,217,.6);
}
.model-sub {
  font-size: 13px; color: rgba(126,184,218,.6);
  letter-spacing: 3px; margin-top: 4px;
  text-shadow: 0 0 12px rgba(74,144,217,.4);
}

/* Login panel */
.login-center { flex: 0 0 36%; display: flex; align-items: center; justify-content: center; z-index: 10; margin-right: 4%; }

/* Animated grid background */
.bg-mesh {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(59,125,216,.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59,125,216,.05) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: meshMove 20s linear infinite;
}
@keyframes meshMove {
  0% { background-position: 0 0, 0 0; }
  100% { background-position: 50px 50px, 50px 50px; }
}

/* Floating light orbs */
.orb {
  position: absolute; border-radius: 50%;
  filter: blur(80px); opacity: 0.15;
  animation: orbFloat 12s ease-in-out infinite;
}
.orb-1 { width:400px; height:400px; background:radial-gradient(circle,#4a90d9,transparent); top:-10%; left:-5%; animation-delay:0s; }
.orb-2 { width:300px; height:300px; background:radial-gradient(circle,#2c5fa1,transparent); bottom:-10%; right:-5%; animation-delay:-4s; }
.orb-3 { width:250px; height:250px; background:radial-gradient(circle,#7eb8da,transparent); top:50%; left:60%; animation-delay:-8s; }
@keyframes orbFloat {
  0%,100% { transform: translate(0,0) scale(1); }
  33% { transform: translate(30px,-20px) scale(1.1); }
  66% { transform: translate(-20px,15px) scale(0.9); }
}

.glass-panel {
  width: 440px; padding: 48px 44px 40px;
  border-radius: 24px;
  background: rgba(12, 24, 48, 0.6);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(74,144,217,0.18);
  position: relative; overflow: hidden;
  box-shadow: 0 8px 48px rgba(0,0,0,.4), 0 0 60px rgba(74,144,217,0.08), inset 0 0 40px rgba(74,144,217,0.02);
}

/* Edge scan line */
.edge-scan {
  position: absolute; top: 0; left: -100%; width: 50%; height: 2px;
  background: linear-gradient(90deg, transparent, rgba(126,184,218,.8), rgba(74,144,217,.3), transparent);
  animation: edgeScan 3.5s ease-in-out infinite;
}
.edge-scan-bottom { top: auto; bottom: 0; animation-delay: 1.75s; }
@keyframes edgeScan {
  0% { left: -50%; opacity: 0; }
  25% { opacity: 1; }
  75% { opacity: 1; }
  100% { left: 120%; opacity: 0; }
}

/* Header */
.panel-header { text-align: center; margin-bottom: 36px; }
.sys-name {
  font-size: 28px; font-weight: 700; color: #c8dff5;
  letter-spacing: 5px; margin-bottom: 6px;
  text-shadow: 0 0 24px rgba(74,144,217,.5);
}
.sys-sub {
  font-size: 13px; color: rgba(126,184,218,.55); letter-spacing: 3px;
}

/* Inputs */
.glass-input :deep(.el-input__wrapper) {
  background: rgba(20,40,70,.3) !important;
  border: 1px solid rgba(74,144,217,.15) !important;
  border-radius: 12px !important; box-shadow: none !important;
  transition: all .35s ease;
}
.glass-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(74,144,217,.4) !important;
  box-shadow: 0 0 12px rgba(74,144,217,.08) !important;
}
.glass-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(74,144,217,.6) !important;
  box-shadow: 0 0 20px rgba(74,144,217,.12) !important;
}
.glass-input :deep(.el-input__inner) { color: #c8dff5 !important; font-size: 15px; }
.glass-input :deep(.el-input__inner::placeholder) { color: rgba(126,184,218,.35) !important; }
.glass-input :deep(.el-input__prefix) { color: rgba(126,184,218,.45) !important; }

/* Button */
.login-btn {
  width: 100%; height: 48px; font-size: 17px; letter-spacing: 8px; margin-top: 12px;
  background: linear-gradient(135deg, #2c5fa1, #1a3a6c) !important;
  border: 1px solid rgba(74,144,217,.25) !important;
  border-radius: 28px !important;
  transition: all .4s ease !important;
}
.login-btn:hover {
  background: linear-gradient(135deg, #3b7dd8, #2c5fa1) !important;
  border-color: rgba(126,184,218,.5) !important;
  box-shadow: 0 0 36px rgba(74,144,217,.25), 0 4px 12px rgba(0,0,0,.3) !important;
  transform: translateY(-2px);
}

/* Tips */
.tips { margin-top: 30px; padding-top: 20px; border-top: 1px solid rgba(74,144,217,.1); text-align: center; }
.tips-title { font-size: 13px; font-weight: 600; color: rgba(126,184,218,.6); margin-bottom: 10px; }
.tips p { font-size: 12px; color: rgba(126,184,218,.4); line-height: 1.9; margin: 0; }
</style>
