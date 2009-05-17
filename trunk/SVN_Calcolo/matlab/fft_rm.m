% Algoritmo di Cooley-Tukey per il calcolo della FFT (repeated multiplication)
%
% Descrizione:
% Calcola la DFT di un vettore x di dimensione n=p^t utilizzando l'algoritmo
% di Cooley-Tukey e il metodo della moltiplicazione ripetuta per il calcolo dei
% twiddle factors.
%
% Uso:
% X = rmctfft(x)
%
% Parametri di ingresso:
% x: vettore colonna di ingresso
% p: base della potenza n=p^t
% t: esponente della potenza n=p^t
%
% Parametri di uscita:
% X: vettore colonna DFT di x

function X = fft_rm(x,p,t)

n = length(x);

R = sparse(n,n);

% Calcolo sqrt(p)*Fp
Fp = zeros(p);
clear i;
armFp = exp(-2*pi*i/p);         
for k=1:p
    for j=1:p
        Fp(k,j) = armFp^((k-1)*(j-1));
    end
end
Fp = sqrt(p)*Fp;

% Calcolo matrice di permutazione (sparsa)
for k=1:n
    ks = adic(k-1,p);
    rev = 0;
    r = length(ks);
    for s=1:r
        rev = rev+ks(s)*p^(t-s);
    end
    for l=1:n
        if rev-l+1==0
            R(k,l) = 1;
        end
    end
end

X = R*x;

% Calcolo delle twiddle and butterfly matrices e determinazione X
for j=1:t
    or = p^(t-j);
    I1 = sparse(or,or);
    for k=1:or
        I1(k,k) = 1;
    end
    or = p^(j-1);
    I2 = sparse(or,or);
    for k=1:or
        I2(k,k) = 1;
    end
    B = kron(kron(I1,Fp),I2);
    
    if j==1
        X = B*X;
        continue;
    end
    
    % Calcolo matrici W (repeated multiplication)
    W = sparse(or,or);
    armpj = cos(2*pi/p^j)-i*sin(2*pi/p^j);
    W(1,1) = 1;
    for k=2:or        
        W(k,k) = W(k-1,k-1)*armpj;
    end
    
    M = I2;
    for k=1:p-1
        m = size(M,1);
        for b=m+1:size(W,1)+m
            M(b,b) = W(b-m,b-m)^k;
        end
    end
    T = kron(I1,M);
    
    X = B*T*X;
end

X = 1/sqrt(n)*X;

% ----------------------------------------- %

% Funzione ausiliaria per il calcolo della rappresentazione p-adica di un
% numero intero k

function ks = adic(k,p)

ks = [];
while k>0
    ks = [ks;mod(k,p)];
    k = fix(k/p);
end