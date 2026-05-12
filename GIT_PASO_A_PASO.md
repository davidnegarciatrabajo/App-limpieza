# Git paso a paso (CMD)

Guia rapida para actualizar este proyecto en GitHub sin errores comunes.

## 1) Ir al proyecto

```cmd
cd /d "C:\Users\dngarcia\Proyectos\App-limpieza"
```

## 2) Ver estado actual

```cmd
git status
git branch
```

Si estas en otra rama y queres subir a principal:

```cmd
git checkout main
```

## 3) Traer cambios remotos (recomendado)

```cmd
git pull origin main
```

## 4) Agregar archivos al commit

```cmd
git add -A
```

Recomendado cuando hay una mezcla de:
- archivos nuevos
- archivos modificados
- archivos borrados

`git add` sin nada despues no agrega archivos y muestra:

```cmd
Nothing specified, nothing added.
hint: Maybe you wanted to say 'git add .'?
```

## 5) Crear commit

```cmd
git commit -m "feat: descripcion clara del cambio"
```

Si no hay cambios nuevos, Git va a informar `nothing to commit`.

## 6) Subir a GitHub

```cmd
git push origin main
```

## 7) Verificar resultado

```cmd
git status
git log --oneline -n 5
```

Esperado:
- `working tree clean`
- `Your branch is up to date with 'origin/main'`

---

## Errores comunes y solucion

### A) Comandos pegados en una sola linea

Error tipico:
- `git statusgit log --oneline -n 5`
- `git add .git commit -m "..."`

Solucion:
- Ejecutar **un comando por linea** y presionar Enter entre cada uno.

### B) `fatal: detected dubious ownership`

1. Agregar directorio seguro en Git:

```cmd
git config --global --add safe.directory "C:/Users/dngarcia/Proyectos/App-limpieza"
```

2. Reintentar:

```cmd
git status
```

3. Si persiste por permisos de Windows, en CMD como Administrador:

```cmd
takeown /f "C:\Users\dngarcia\Proyectos\App-limpieza" /r /d y
icacls "C:\Users\dngarcia\Proyectos\App-limpieza" /setowner "DESKTOP-NV4Q9H1\dngarcia" /t
```

---

## Flujo corto diario (recomendado)

```cmd
cd /d "C:\Users\dngarcia\Proyectos\App-limpieza"
git status
git add -A
git commit -m "feat: resumen breve"
git push origin main
git status
```

## Caso real: si Git muestra muchos `M`, `D` y `??`

Eso significa normalmente:
- `M`: archivo modificado
- `D`: archivo borrado
- `??`: archivo nuevo sin seguimiento

En ese caso, el flujo correcto es:

```cmd
git status
git add -A
git status
git commit -m "feat: resumen breve"
git push origin main
```

Si `git status` despues de `git add -A` muestra archivos en verde bajo `Changes to be committed`, ya esta listo para el commit.

## Nota

Si queres un flujo mas profesional, usar:
- rama de trabajo (`feature/...`)
- push de esa rama
- Pull Request a `main`

---

## Copiar y pegar

Usar este bloque tal como esta, ejecutando una linea por vez:

```cmd
cd /d "C:\Users\dngarcia\Proyectos\App-limpieza"
git status
git add -A
git status
git commit -m "feat: actualiza app"
git push origin main
git status
git log --oneline -n 5
```

Resultado esperado al final:
- `Your branch is up to date with 'origin/main'`
- `nothing to commit, working tree clean`
  