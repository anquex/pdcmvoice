% Verifica dimostrazione Gentleman-Sande

clear all;
close all;
clc;
A=[1 2 3 4 5 6 7 8]';
n=8; %lunghezza dft
S=zeros(n,n);
C=zeros(n,n);

for k=1:n
    for j=1:n
        C(k,j)=cos(2*pi*(k-1)*(j-1)/n);
        S(k,j)=sin(2*pi*(k-1)*(j-1)/n);
    end
end

O=[C S; -S C];

DFT2=O*[A;zeros(2*n-length(A),1)];
DFT=DFT2(1:n)+i*DFT2(n+1:2*n)

FFT=fft(A)


    