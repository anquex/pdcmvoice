clear all;
close all;
clc;

N=7; % numero di punti
l=2;  % potenza di partenza
base=2;
punti=base*ones(1,N);
esponente=linspace(l,N+l-1,N);
punti=punti.^esponente;

norm2MATFFT=zeros(1,N);
norm2FFTDC=zeros(1,N);
norm2FFTRM=zeros(1,N);
norm2FFTVS=zeros(1,N);
norm2FFTFR=zeros(1,N);
norma=zeros(1,N);
norma2DFT=zeros(1,N);
for i=1:N
    vettore=rand(1,punti(i));
    l=length(vettore);
    norma(i)=norm(vettore);
%FFT MATLAB FFT
%     FFT=fft(vettore);
%     IFFT=ifft(FFT);
%     clear FFT;
%     norm2MATFFT(i)=norm(vettore-IFFT)/norma(i);
%     clear IFFT;
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
%FFT DIT2-RM-ITER
    FFT=fft_rm_iter(vettore,-1);
    IFFT=fft_rm_iter(FFT,1)/l;
    clear FFT;
    norm2FFTRM(i)=norm(vettore-IFFT)/norma(i);
    clear IFFT;
%FFT DIT2-FR-ITER
%     FFT=fft_fr_iter(vettore,-1);
%     IFFT=fft_fr_iter(FFT,1)/l;
%     clear FFT;
%     norm2FFTFR(i)=norm(vettore-IFFT)/norma(i);
%     clear IFFT;
% %DFT
%     DFT=dft(vettore);
%     IDFT=idft(DFT);
%     clear DFT;
%     norm2DFT(i)=norm(vettore-IDFT)/norma(i);
%     clear IDFT;
     i
end 
%plot(esponente,norm2FFT,'b',esponente,norm2DFT,'r');
semilogy(esponente,norm2FFTDC,'bv',esponente,norm2FFTRM,'rv',esponente,norm2FFTVS,'mv');
legend('errore FFT-DC','errore FFT-RM','errore FFT-VS','errore MATLAB-FFT','Location','NorthWest');
grid on;
xlabel('esponente');
ylabel('errore');
title('Norma 2 dell''errore');

hold on;
semilogy(esponente,norm2FFTDC,'b',esponente,norm2FFTRM,'r',esponente,norm2FFTVS,'black');
