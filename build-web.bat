@echo off
echo Building TypeScript Web Viewer...
cd web
call npm install
call npm run build
echo.
echo Web viewer built successfully!
echo To serve the web viewer, run: cd web && npm run serve
echo Then open http://localhost:8080 in your browser
pause
