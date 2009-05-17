clear all;
close all;
clc;

N=16; % numero di punti
l=2;  % potenza di partenza
base=2;
punti=base*ones(1,N);
esponente=linspace(l,N+l-1,N);
punti=punti.^esponente;

norm2FFTDC=zeros(1,N);
norm2FFTVS=zeros(1,N);
norma=zeros(1,N);
for i=1:N
    vettore=rand(1,punti(i));
    l=length(vettore);
    norma(i)=norm(vettore);
%FFT DIT2-DC-ITER
    FFT=fft_dc_iter(vettore,-1);
    IFFT=fft_dc_iter(FFT,1)/l;
    clear FFT;
    norm2FFTDC(i)=norm(vettore-IFFT)/norma(i);
    clear IFFT;
%FFT DIT2-VS-ITER
    FFT=fft_vs_iter(vettore,-1);
    IFFT=fft_vs_iter(FFT,1)/l;
    clear FFT;
    norm2FFTVS(i)=norm(vettore-IFFT)/norma(i);
    clear IFFT;
    i
end 
%plot(esponente,norm2FFT,'b',esponente,norm2DFT,'r');
plot(esponente,norm2FFTDC,'b',esponente,norm2FFTVS,'black');
legend('errore FFT-DC','errore FFT-VS','Location','NorthWest');
grid on;
xlabel('esponente');
ylabel('errore');
title('Confronto DC-VS');
