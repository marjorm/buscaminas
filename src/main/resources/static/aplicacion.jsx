const { useState, useEffect, useCallback } = React;

async function api(ruta, opciones = {}) {
  const metodo = (opciones.method || 'GET').toUpperCase();
  const encabezados = { 'Accept': 'application/json' };
  if (opciones.body !== undefined) encabezados['Content-Type'] = 'application/json';
  if (!['GET', 'HEAD', 'OPTIONS'].includes(metodo)) {
    const respuestaCsrf = await fetch('/api/auth/csrf', { credentials: 'same-origin', cache: 'no-store' });
    if (!respuestaCsrf.ok) throw new Error('No se pudo obtener el token de seguridad');
    const csrf = await respuestaCsrf.json();
    encabezados['X-XSRF-TOKEN'] = csrf.token;
  }
  const respuesta = await fetch(ruta, {
    method: metodo,
    credentials: 'same-origin',
    headers: encabezados,
    body: opciones.body === undefined ? undefined : JSON.stringify(opciones.body)
  });
  if (respuesta.status === 204) return null;
  const tipo = respuesta.headers.get('content-type') || '';
  const datos = tipo.includes('application/json') ? await respuesta.json() : await respuesta.text();
  if (!respuesta.ok) {
    const mensaje = typeof datos === 'object' ?
      (datos.detail || datos.message || datos.mensaje || datos.error) : datos;
    const error = new Error(mensaje || `Solicitud rechazada (HTTP ${respuesta.status})`);
    error.status = respuesta.status;
    throw error;
  }
  return datos;
}

const formatearTiempo = ms => {
  if (ms === null || ms === undefined) return '—';
  const centesimasTotales = Math.floor(Math.max(0, Number(ms) || 0) / 10);
  const minutos = Math.floor(centesimasTotales / 6000);
  const segundos = Math.floor(centesimasTotales / 100) % 60;
  const centesimas = centesimasTotales % 100;
  return `${String(minutos).padStart(2, '0')}:${String(segundos).padStart(2, '0')}.${String(centesimas).padStart(2, '0')}`;
};

/** Reloj visual fluido. El resultado oficial sigue calculándose en el servidor. */
function Reloj({ partida }) {
  const [tiempo, setTiempo] = useState(partida.tiempoMs || 0);
  useEffect(() => {
    const base = Number(partida.tiempoMs) || 0;
    setTiempo(base);
    if (partida.estado !== 'EN_CURSO') return;
    const inicio = performance.now();
    const intervalo = setInterval(() => {
      setTiempo(base + Math.max(0, performance.now() - inicio));
    }, 50);
    return () => clearInterval(intervalo);
  }, [partida.id, partida.estado, partida.tiempoMs]);
  return <strong aria-live="off">{formatearTiempo(tiempo)}</strong>;
}
const formatearFecha = fecha => fecha ? new Date(fecha).toLocaleString('es-CR') : '—';
const texto = estado => ({GANADA:'Ganada',PERDIDA:'Perdida',EN_CURSO:'En curso',ABANDONADA:'Abandonada'})[estado] || estado;

function Alerta({ error, mensaje }) {
  if (!error && !mensaje) return null;
  return <div role="status" className={`aviso ${error ? 'error' : ''}`}>{error || mensaje}</div>;
}

function Acceso({ modo, alCompletar }) {
  const [nombre,setNombre] = useState('');
  const [nombreUsuario,setNombreUsuario] = useState('');
  const [email,setEmail] = useState('');
  const [password,setPassword] = useState('');
  const [confirmarPassword,setConfirmarPassword] = useState('');
  const [error,setError] = useState('');
  const [cargando,setCargando] = useState(false);
  const esRegistro = modo === 'registro';

  const enviar = async evento => {
    evento.preventDefault();
    setError('');
    if (esRegistro && password !== confirmarPassword) {
      setError('Las contraseñas no coinciden');
      return;
    }
    if (esRegistro && (!/(?=.*[A-Za-z])(?=.*\d).{8,}/.test(password))) {
      setError('La contraseña necesita mínimo 8 caracteres, una letra y un número');
      return;
    }
    setCargando(true);
    try {
      if (esRegistro) {
        await api('/api/auth/registro', { method:'POST', body:{ nombre, nombreUsuario, email, password, confirmarPassword } });
      }
      const usuario = await api('/api/auth/login', { method:'POST', body:{ nombreUsuario, password } });
      alCompletar(usuario);
    } catch (e) { setError(e.message); }
    finally { setCargando(false); }
  };
  return <section className="panel centrado">
    <h1>{esRegistro ? 'Crea tu cuenta' : 'Inicia sesión'}</h1>
    <p className="subtitulo">{esRegistro ? 'Regístrate para guardar tus partidas y entrar al ranking.' : 'Vuelve a tu tablero y supera tus mejores tiempos.'}</p>
    <form className="formulario" onSubmit={enviar}>
      {esRegistro && <label className="campo">Nombre completo<input required maxLength="80" value={nombre} onChange={e=>setNombre(e.target.value)}/></label>}
      <label className="campo">Nombre de usuario<input required maxLength="40" autoComplete="username" value={nombreUsuario} onChange={e=>setNombreUsuario(e.target.value)}/></label>
      {esRegistro && <label className="campo">Correo electrónico<input type="email" required maxLength="120" value={email} onChange={e=>setEmail(e.target.value)}/></label>}
      <label className="campo">Contraseña<input type="password" required minLength={esRegistro ? 8 : 1} autoComplete={esRegistro ? 'new-password' : 'current-password'} value={password} onChange={e=>setPassword(e.target.value)}/></label>
      {esRegistro && <label className="campo">Confirmar contraseña<input type="password" required minLength="8" autoComplete="new-password" value={confirmarPassword} onChange={e=>setConfirmarPassword(e.target.value)}/></label>}
      <Alerta error={error}/>
      <button className="boton" disabled={cargando} type="submit">{cargando ? 'Verificando…' : esRegistro ? 'Registrarme y entrar' : 'Entrar'}</button>
    </form>
  </section>;
}

function Tablero({ partida, ejecutar, ocupado }) {
  if (!partida) return null;
  const activa = partida.estado === 'EN_CURSO';
  return <div>
    <div className="cab-tablero">
      <div><h2>Tablero {partida.nivel.toLowerCase()}</h2><span className="muted">{partida.filas} × {partida.columnas} casillas</span></div>
      <div className="indicadores">
        <div className="pill">Minas <strong>{partida.minas}</strong></div>
        <div className="pill">Banderas <strong>{partida.banderas}</strong></div>
        <div className="pill">Movimientos <strong>{partida.movimientos}</strong></div>
        <div className="pill">Tiempo <Reloj partida={partida}/></div>
      </div>
    </div>
    {!activa && <p className={`estado-fin ${partida.resultado === 'PERDIDA' ? 'perdida' : ''}`}>{partida.resultado === 'GANADA' ? '¡Ganaste! Descubriste todas las casillas seguras.' : partida.resultado === 'PERDIDA' ? 'Perdiste: descubriste una mina.' : 'Partida abandonada.'}</p>}
    <div className="tablero-scroll">
      <div className="tablero" style={{gridTemplateColumns:`repeat(${partida.columnas},32px)`}} role="grid" aria-label="Tablero de Buscaminas">
        {partida.casillas.map((fila, f) => fila.map((casilla,c) => {
          const abierta = casilla.estado === 'ABIERTA';
          const numero = casilla.numero;
          const contenido = casilla.estado === 'BANDERA' ? '⚑' : casilla.estado === 'EXPLOTADA' ? '✹' : abierta && numero > 0 ? numero : '';
          return <button key={`${f}-${c}`} type="button" role="gridcell" className={`casilla ${casilla.estado.toLowerCase()} ${abierta ? `n${numero}` : ''}`}
            aria-label={`Fila ${f+1}, columna ${c+1}: ${casilla.estado.toLowerCase()}${abierta ? ` ${numero}` : ''}`}
            title={`Fila ${f+1}, columna ${c+1}. Clic para revelar, clic derecho para bandera.`}
            disabled={!activa || ocupado || abierta || casilla.estado==='EXPLOTADA'}
            onClick={()=> ejecutar('REVELAR',f,c)}
            onContextMenu={e=>{e.preventDefault();if(activa && !ocupado && !abierta) ejecutar('BANDERA',f,c);}}>{contenido}</button>;
        }))}
      </div>
    </div>
    <p className="pie-tablero">Clic izquierdo: revelar. Clic derecho: colocar o quitar bandera. Marca con banderas las casillas que consideres peligrosas.</p>
  </div>;
}

function Juego({ alCambiar }) {
  const [niveles,setNiveles] = useState([]);
  const [partida,setPartida] = useState(null);
  const [error,setError] = useState('');
  const [ocupado,setOcupado] = useState(false);
  const [seleccion,setSeleccion] = useState('PRINCIPIANTE');
  const [modoBandera,setModoBandera] = useState(false);

  const cargar = useCallback(async () => {
    try {
      const opciones = await api('/api/niveles');
      setNiveles(opciones);
      try { setPartida(await api('/api/partidas/actual')); }
      catch (e) { if (e.status !== 404) throw e; }
    } catch (e) { setError(e.message); }
  }, []);
  useEffect(()=>{cargar();},[cargar]);


  const nueva = async nivel => {
    setOcupado(true);setError('');
    try {setPartida(await api('/api/partidas',{method:'POST',body:{nivel}}));setModoBandera(false);alCambiar();}
    catch(e){setError(e.message);}finally{setOcupado(false);}
  };
  const ejecutar = async(tipo,fila,columna) => {
    if (!partida || ocupado) return;
    setOcupado(true);setError('');
    try {setPartida(await api(`/api/partidas/${partida.id}/acciones`,{method:'POST',body:{tipo:modoBandera ? 'BANDERA' : tipo,fila,columna}}));alCambiar();}
    catch(e){setError(e.message);}finally{setOcupado(false);}
  };
  const abandonar = async()=>{
    if(!partida || !window.confirm('¿Abandonar la partida actual?'))return;
    setOcupado(true);setError('');
    try {setPartida(await api(`/api/partidas/${partida.id}/abandonar`,{method:'POST'}));alCambiar();}
    catch(e){setError(e.message);}finally{setOcupado(false);}
  };
  return <section className="panel">
    <h1>Jugar Buscaminas</h1>
    <p className="subtitulo">Encuentra todas las casillas seguras sin descubrir una mina. Tu avance se guarda para que puedas continuar cuando quieras.</p>
    <Alerta error={error}/>
    {(!partida || partida.estado !== 'EN_CURSO') && <div className="tarjetas" style={{margin:'18px 0'}}>
      {niveles.map(n=><button className="tarjeta-nivel" key={n.id} type="button" disabled={ocupado} onClick={()=>nueva(n.id)}><strong>{n.nombre}</strong><small>{n.filas} × {n.columnas} · {n.minas} minas</small></button>)}
    </div>}
    {partida && <>
      {partida.estado === 'EN_CURSO' && <div className="acciones" style={{marginBottom:14}}>
        <button className={`boton ${modoBandera?'':'secundario'}`} onClick={()=>setModoBandera(v=>!v)} type="button">{modoBandera ? 'Modo bandera: activado' : 'Activar modo bandera'}</button>
        <button className="boton peligro" disabled={ocupado} onClick={abandonar} type="button">Abandonar partida</button>
      </div>}
      <Tablero partida={partida} ejecutar={ejecutar} ocupado={ocupado}/>
    </>}
    {!partida && niveles.length>0 && <p className="muted">Selecciona un nivel para empezar.</p>}
  </section>;
}

function Historial({usuario, refresco}){
  const [filas,setFilas] = useState([]);
  const [error,setError] = useState('');
  const [cargando,setCargando] = useState(true);
  useEffect(()=>{
    let activo = true;
    setCargando(true);
    api(`/api/usuarios/${usuario.id}/partidas`).then(r=>{if(activo)setFilas(r);}).catch(e=>{if(activo)setError(e.message);}).finally(()=>{if(activo)setCargando(false);});
    return()=>{activo=false;};
  },[usuario.id,refresco]);
  return <section className="panel"><h1>Mi historial</h1><p className="subtitulo">Partidas guardadas con fecha, duración y movimientos.</p><Alerta error={error}/>{cargando?<p>Cargando…</p>:filas.length===0?<p className="muted">Todavía no tienes partidas.</p>:<div className="tabla-scroll"><table className="tabla"><thead><tr><th>Partida</th><th>Nivel</th><th>Estado</th><th>Movimientos</th><th>Tiempo</th><th>Inicio</th><th>Fin</th></tr></thead><tbody>{filas.map(p=><tr key={p.id}><td>#{p.id}</td><td>{p.nivel}</td><td><span className="etiqueta">{texto(p.resultado || p.estado)}</span></td><td>{p.movimientos}</td><td>{formatearTiempo(p.tiempoMs)}</td><td>{formatearFecha(p.fechaInicio)}</td><td>{formatearFecha(p.fechaFin)}</td></tr>)}</tbody></table></div>}</section>;
}

function Ranking({usuario,refresco}){
  const [filas,setFilas] = useState([]);
  const [nivel,setNivel] = useState('PRINCIPIANTE');
  const [error,setError] = useState('');
  useEffect(()=>{let activo=true;api('/api/ranking').then(r=>{if(activo)setFilas(r);}).catch(e=>{if(activo)setError(e.message);});return()=>{activo=false;};},[refresco]);
  const orden = filas.filter(f=>f.nivel===nivel).sort((a,b)=>Number(a.mejorTiempoMs)-Number(b.mejorTiempoMs));
  const posicion=orden.findIndex(f=>Number(f.usuarioId)===Number(usuario.id));
  return <section className="panel"><h1>Ranking global</h1><p className="subtitulo">Mejores tiempos de partidas ganadas, clasificados por nivel.</p><Alerta error={error}/><label className="campo" style={{maxWidth:250,marginBottom:15}}>Selecciona nivel<select value={nivel} onChange={e=>setNivel(e.target.value)}>{['PRINCIPIANTE','INTERMEDIO','EXPERTO'].map(n=><option key={n}>{n}</option>)}</select></label>{posicion>=0&&<p className="aviso">Tu posición en {nivel.toLowerCase()}: #{posicion+1}</p>}<div className="tabla-scroll"><table className="tabla"><thead><tr><th>Posición</th><th>Jugador</th><th>Mejor tiempo</th><th>Partidas terminadas</th></tr></thead><tbody>{orden.map((p,i)=><tr key={`${p.usuarioId}-${p.nivel}`} className={Number(p.usuarioId)===Number(usuario.id)?'yo':''}><td>#{i+1}</td><td>{p.nombreUsuario}{Number(p.usuarioId)===Number(usuario.id)?' (tú)':''}</td><td>{formatearTiempo(p.mejorTiempoMs)}</td><td>{p.partidasTerminadas}</td></tr>)}</tbody></table>{orden.length===0&&<p className="muted">Todavía no hay tiempos registrados para este nivel.</p>}</div></section>;
}

function App(){
  const [usuario,setUsuario]=useState(null);
  const [vista,setVista]=useState('login');
  const [error,setError]=useState('');
  const [inicializando,setInicializando]=useState(true);
  const [refresco,setRefresco]=useState(0);
  useEffect(()=>{api('/api/auth/me').then(u=>{setUsuario(u);setVista('juego');}).catch(()=>{}).finally(()=>setInicializando(false));},[]);
  const entrar = u=>{setUsuario(u);setVista('juego');setError('');setRefresco(v=>v+1);};
  const salir = async()=>{try{await api('/api/auth/logout',{method:'POST'});setUsuario(null);setVista('login');setError('');}catch(e){setError(e.message);}};
  return <>
    <header className="cabecera"><div className="contenedor barra"><div className="marca">✹ <span>MINAS</span> / BUSCAMINAS</div>{usuario&&<div className="usuario">Jugando como <strong>{usuario.nombreUsuario}</strong><button type="button" onClick={salir}>Cerrar sesión</button></div>}</div></header>
    {usuario&&<nav className="contenedor navegacion" aria-label="Navegación">{[['juego','Juego'],['historial','Historial'],['ranking','Ranking']].map(([id,nombre])=><button key={id} type="button" className={vista===id?'activo':''} onClick={()=>{setVista(id);setError('');}}>{nombre}</button>)}</nav>}
    <main className="contenedor contenido"><Alerta error={error}/>{inicializando?<p className="cargando">Recuperando sesión…</p>:usuario?(
      vista==='juego'?<Juego alCambiar={()=>setRefresco(v=>v+1)}/>:
      vista==='historial'?<Historial usuario={usuario} refresco={refresco}/>:<Ranking usuario={usuario} refresco={refresco}/>
    ):<><Acceso key={vista} modo={vista} alCompletar={entrar}/><div className="acciones" style={{justifyContent:'center'}}><button className="boton secundario" type="button" onClick={()=>setVista(v=>v==='login'?'registro':'login')}>{vista==='login'?'Crear cuenta nueva':'Ya tengo cuenta'}</button></div></>}</main>
    <footer className="footer">Buscaminas · Pon a prueba tu lógica y encuentra todas las casillas seguras</footer>
  </>;
}

ReactDOM.createRoot(document.getElementById('root')).render(<App/>);
