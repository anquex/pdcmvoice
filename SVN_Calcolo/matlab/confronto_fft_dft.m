clear all;
close all;
clc;

N=6; % numero di punti
l=2;  % potenza di partenza
base=2;
punti=base*ones(1,N);
esponente=linspace(l,N+l-1,N);
punti=punti.^esponente;

norm2MATFFT=zeros(1,N);
norm2FFTDC=zeros(1,N);
norm2FFTRM=zeros(1,N);
norm2FFTVS=zeros(1,N);
norma=zeros(1,N);
norm2DFT=zeros(1,N);
for i=1:N
    vettore=rand(1,punti(i));
    l=length(vettore);
    norma(i)=norm(vettore);
%FFT MATLAB FFT
    FFT=fft(vettore);
    IFFT=ifft(FFT);
    clear FFT;
    norm2MATFFT(i)=norm(vettore-IFFT)/norma(i);
    clear IFFT;
%DFT
    DFT=dft(vettore);
    IDFT=idft(DFT);
    clear DFT;
    norm2DFT(i)=norm(vettore-IDFT)/norma(i);
    clear IDFT;
    i
end 
%plot(esponente,norm2FFT,'b',esponente,norm2DFT,'r');
semilogy(esponente,norm2MATFFT,'b',esponente,norm2DFT,'r');
legend('errore FFT','errore DFT','Location','NorthWest');
grid on;
xlabel('esponente');
ylabel('errore');
title('Norma 2 dell''errore');
