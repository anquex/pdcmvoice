clear all;
close all;
clc;

N=14; % numero di punti
l=2;  % potenza di partenza
punti=2*ones(1,N);
esponente=linspace(l,N+l-1,N);
punti=punti.^esponente;
boundDFT=inline('1.06.*(2.*x).^(3/2).*2^-52','x');
boundFFT=inline('1.06.*log2(2*x).*4.^(3/2).*2^-52','x');

norm2FFT=zeros(1,N);
norm2DFT=zeros(1,N);
norma=zeros(1,N);
for i=1:N
    vettore=rand(1,punti(i));
    norma(i)=norm(vettore);
    FFT=fft_dc_iter(vettore,-1);
    IFFT=fft_dc_iter(FFT,1)/length(vettore);
    clear FFT;
    norm2FFT(i)=norm(vettore-IFFT)/norma(i);
    clear IFFT;
    DFT=dft(vettore);
    IDFT=idft(DFT);
    clear DFT;
    norm2DFT(i)=norm(vettore-IDFT')/norma(i);
    clear IDFT;
    i
end

semilogy(esponente,norm2DFT,'b',esponente,boundDFT(punti),'black--',esponente,norm2FFT,'r',esponente,boundFFT(punti),'blacko');
legend('errore reale DFT','Upper Bound errore DFT','errore reale FFT','Upper Bound errore FFT','Location','NorthWest');
grid on;
xlabel('esponente');
ylabel('errore');
title('Riepilogo DFT - FFT -Worst Case Bound');
