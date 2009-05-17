clear all;
close all;
clc;

N=8; % numero di punti
l=2;  % potenza di partenza
base=2;
punti=base*ones(1,N);
esponente=linspace(l,N+l-1,N);
punti=punti.^esponente;

norm2FFTDC=zeros(1,N);
norm2FFTRM=zeros(1,N);
norma=zeros(1,N);
for i=1:N
    vettore=rand(punti(i),1);
    norma(i)=norm(vettore);
    FFT=fft_dc(vettore,base,esponente(i));
    IFFT=rev(ifft_dc(FFT,base,esponente(i)));
    clear FFT;
    norm2FFTDC(i)=norm(vettore-IFFT);
    clear IFFT;
    FFT=fft_rm(vettore,base,esponente(i));
    IFFT=rev(ifft_rm(FFT,base,esponente(i)));
    clear FFT;
    norm2FFTRM(i)=norm(vettore-IFFT);
    clear IFFT;
    i
end
%plot(esponente,norm2FFT,'b',esponente,norm2DFT,'r');
semilogy(esponente,norm2FFTDC,'b',esponente,norm2FFTRM,'r');
legend('errore FFT-DC','errore DFT-RM','Location','SouthEast');
grid on;
xlabel('esponente');
ylabel('errore');
title('Norma 2 dell''errore');
