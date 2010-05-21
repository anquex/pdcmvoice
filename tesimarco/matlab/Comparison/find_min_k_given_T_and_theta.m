function k=find_min_k_given_T_and_theta(n,D,theta)

% k=find_min_k_given_T(n,D)
%
% Trova il minimo k per cui il vincolo n/k<=n<=kn � rispettato con
% probabilit� maggiore di theta. la distribuzione D � funzione di T, per
% cui T � implicitamente presente

sx=n;
dx=n;
prob=D(n);
while(prob<theta)
    %allarga l'intervallo
    sxn=sx-1;
    dxn=dx+1;
    % calcola i k associati
    ksx=n/sxn;
    kdx=dxn/n;
    %trova il k minimo tra i 2
    k=min(ksx,kdx);
    %determina il nuovo intervallo
    if (k==ksx)
        sx=sxn;
        prob=prob+D(sx);
    else
        dx=dxn;
        prob=prob+D(dx);
    end 
end