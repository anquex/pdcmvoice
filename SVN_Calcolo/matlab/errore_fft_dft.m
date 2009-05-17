clear all;
close all;
clc;

N=16; % numero di punti
l=2;  % potenza di partenza
punti=2*ones(1,N);
esponente=linspace(l,N+l-1,N);
punti=punti.^esponente;

% norm2FFT=zeros(1,N);
% norm2DFT=zeros(1,N);
% norma=zeros(1,N);
% for i=1:N
%     vettore=rand(1,punti(i));
%     norma(i)=norm(vettore);
%     FFT=fft(vettore);
%     IFFT=ifft(FFT);
%     clear FFT;
%     norm2FFT(i)=norm(vettore-IFFT);
%     clear IFFT;
%     DFT=dft(vettore);
%     IDFT=idft(DFT);
%     clear DFT;
%     norm2DFT(i)=norm(vettore-IDFT);
%     clear IDFT;
%     i
% end
% %plot(esponente,norm2FFT,'b',esponente,norm2DFT,'r');
% semilogy(esponente,norm2FFT,'b',esponente,norm2DFT,'r');
% legend('errore FFT','errore DFT','Location','SouthEast');
% grid on;
% xlabel('esponente');
% ylabel('errore');
% title('Norma 2 dell''errore');

%DFT_BOUND=inline('1.06*sqrt(x).*(2.*x).^(3/2)*2^-52','x');
DFT_BOUND=inline('1.06.*(2.*x).^(3/2)*2^-52','x');
dft_bound=DFT_BOUND(punti);
semilogy(esponente,norm2DFT,'b',esponente,dft_bound,'g');
legend('errore reale DFT','Upper Bound errore DFT','Location','SouthEast');
grid on;
xlabel('esponente');
ylabel('errore');
title('Confronto Bound DFT');
