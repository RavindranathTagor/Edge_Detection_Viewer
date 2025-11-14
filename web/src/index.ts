interface FrameData {
    imageData: string;
    width: number;
    height: number;
    fps: number;
    mode: 'raw' | 'edge';
    timestamp: number;
}

class EdgeDetectionViewer {
    private imageElement: HTMLImageElement;
    private placeholderElement: HTMLElement;
    private fpsElement: HTMLElement;
    private resolutionElement: HTMLElement;
    private modeElement: HTMLElement;
    private statusElement: HTMLElement;
    
    private currentMode: 'raw' | 'edge' = 'edge';
    private isConnected: boolean = false;
    private frameCount: number = 0;
    private lastUpdateTime: number = Date.now();

    constructor() {
        this.imageElement = document.getElementById('processedImage') as HTMLImageElement;
        this.placeholderElement = document.getElementById('placeholder') as HTMLElement;
        this.fpsElement = document.getElementById('fpsValue') as HTMLElement;
        this.resolutionElement = document.getElementById('resolutionValue') as HTMLElement;
        this.modeElement = document.getElementById('modeValue') as HTMLElement;
        this.statusElement = document.getElementById('connectionStatus') as HTMLElement;

        this.initializeControls();
        this.simulateConnection();
        
        // Auto-load sample frame on startup
        setTimeout(() => {
            this.loadSampleFrame();
        }, 500);
    }

    private initializeControls(): void {
        const loadSampleBtn = document.getElementById('loadSampleBtn');
        const toggleModeBtn = document.getElementById('toggleModeBtn');
        const refreshBtn = document.getElementById('refreshBtn');

        loadSampleBtn?.addEventListener('click', () => this.loadSampleFrame());
        toggleModeBtn?.addEventListener('click', () => this.toggleMode());
        refreshBtn?.addEventListener('click', () => this.refreshStats());
    }

    private async loadSampleFrame(): Promise<void> {
        const sampleFrames = {
            edge: this.generateEdgeDetectionSample(),
            raw: this.generateRawSample()
        };

        const frameData: FrameData = {
            imageData: sampleFrames[this.currentMode],
            width: 640,
            height: 480,
            fps: 15.0 + Math.random() * 15,
            mode: this.currentMode,
            timestamp: Date.now()
        };

        this.displayFrame(frameData);
        this.isConnected = true;
        this.updateConnectionStatus();
    }

    private displayFrame(frame: FrameData): void {
        this.imageElement.src = frame.imageData;
        this.imageElement.style.display = 'block';
        this.placeholderElement.style.display = 'none';

        this.fpsElement.textContent = frame.fps.toFixed(1);
        this.resolutionElement.textContent = `${frame.width}x${frame.height}`;
        this.modeElement.textContent = frame.mode === 'edge' ? 'Edge' : 'Raw';

        this.frameCount++;
    }

    private toggleMode(): void {
        this.currentMode = this.currentMode === 'edge' ? 'raw' : 'edge';
        this.modeElement.textContent = this.currentMode === 'edge' ? 'Edge' : 'Raw';
        
        if (this.imageElement.style.display !== 'none') {
            this.loadSampleFrame();
        }
    }

    private refreshStats(): void {
        const currentTime = Date.now();
        const elapsed = (currentTime - this.lastUpdateTime) / 1000;
        
        if (elapsed > 0 && this.frameCount > 0) {
            const calculatedFps = this.frameCount / elapsed;
            this.fpsElement.textContent = calculatedFps.toFixed(1);
            
            this.frameCount = 0;
            this.lastUpdateTime = currentTime;
        }
    }

    private updateConnectionStatus(): void {
        if (this.isConnected) {
            this.statusElement.textContent = 'Connected';
            this.statusElement.className = 'connection-status status-connected';
        } else {
            this.statusElement.textContent = 'Disconnected';
            this.statusElement.className = 'connection-status status-disconnected';
        }
    }

    private simulateConnection(): void {
        setInterval(() => {
            if (this.isConnected && this.imageElement.style.display !== 'none') {
                const randomFps = 12.0 + Math.random() * 18;
                this.fpsElement.textContent = randomFps.toFixed(1);
            }
        }, 1000);
    }

    private generateEdgeDetectionSample(): string {
        const canvas = document.createElement('canvas');
        canvas.width = 640;
        canvas.height = 480;
        const ctx = canvas.getContext('2d');
        
        if (!ctx) return '';

        ctx.fillStyle = '#000000';
        ctx.fillRect(0, 0, 640, 480);

        ctx.strokeStyle = '#FFFFFF';
        ctx.lineWidth = 2;

        for (let i = 0; i < 50; i++) {
            const x = Math.random() * 640;
            const y = Math.random() * 480;
            const width = Math.random() * 100;
            const height = Math.random() * 100;
            
            ctx.strokeRect(x, y, width, height);
        }

        for (let i = 0; i < 30; i++) {
            const x = Math.random() * 640;
            const y = Math.random() * 480;
            const radius = Math.random() * 50;
            
            ctx.beginPath();
            ctx.arc(x, y, radius, 0, Math.PI * 2);
            ctx.stroke();
        }

        return canvas.toDataURL('image/png');
    }

    private generateRawSample(): string {
        const canvas = document.createElement('canvas');
        canvas.width = 640;
        canvas.height = 480;
        const ctx = canvas.getContext('2d');
        
        if (!ctx) return '';

        const gradient = ctx.createLinearGradient(0, 0, 640, 480);
        gradient.addColorStop(0, '#667eea');
        gradient.addColorStop(1, '#764ba2');
        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, 640, 480);

        ctx.fillStyle = 'rgba(255, 255, 255, 0.3)';
        for (let i = 0; i < 100; i++) {
            const x = Math.random() * 640;
            const y = Math.random() * 480;
            const radius = Math.random() * 30;
            
            ctx.beginPath();
            ctx.arc(x, y, radius, 0, Math.PI * 2);
            ctx.fill();
        }

        return canvas.toDataURL('image/png');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new EdgeDetectionViewer();
});

export {};
