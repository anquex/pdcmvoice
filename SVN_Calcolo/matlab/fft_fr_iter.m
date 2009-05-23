%  Algoritmo di Cooley-Tukey radix-2
%    - implementazione iterativa in-place
%    - calcolo twiddle factors attraverso repeated multiplication
%
%  Descrizione: utilizzabile per calolare ftt/ifft, � necessario
%               scalare la ifft restituita per 1/N
%
%  INPUT
%    x : vettore complesso
%   is : segno dell'esponenziale (+1)  FFT
%                                (-1) IFFT
%
%  OUTPUT
%
%   X  : FFT di x , vettore colonna

function a=fft_fr_iter(a,is)

W=zeros(1,length(a)); %vettore dei twiddle factor

ldn=log2(length(a));
n=length(a);
% Repeated multiplication
W(1)=1;                % omega 0
W(2)=exp(is*2*pi*i/n); % omega 1
t=2*W(2);
for k=3:n
    W(k)=t*real(W(k-1))-real(W(k-2));
    W(k)=W(k)+i*(t*imag(W(k-1))-imag(W(k-2)));
end

% for k=3:n
%     W(k)=t*W(k-1)-W(k-2);
% end

%bit-reversal dell'input
a= bitrevorder(a);

for ldm=1:ldn
   m=2^ldm; %dimensione della fft 
   mh=m/2;  %gli elementi sono suddivisi in n/m gruppi di (m/2)*2 elementi 
   for j=0:mh-1   % Calcolo FFT
      e=W(j*n/m+1); % twiddle factor
      for r=0:m:n-m         
          u=a(r+1+j);
          v=a(r+1+j+mh)*e;
          a(r+1+j)=u+v;
          a(r+1+j+mh)=u-v;
      end
   end  
end